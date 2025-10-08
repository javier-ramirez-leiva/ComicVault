import { Component, HostListener, inject, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ComicsDatabase } from 'interfaces';
import { ComicsService, DownloadService, OrientationService, ReaderSettingsService, WindowService, ZoomService } from 'services';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { combineLatest, EMPTY, forkJoin, Observable, of, Subject } from 'rxjs';
import { debounceTime, delay, distinctUntilChanged, filter, map, startWith, switchMap, tap } from 'rxjs/operators';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BackButtonComponent } from "../back-button/back-button.component";
import { Location } from '@angular/common';
import { SwipeDirective } from 'directives';
import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';
import { computeDoublePages, MinipagesCarouselComponent } from "../minipages-carousel/minipages-carousel.component";
import { ReaderSettingsButtonComponent } from "../reader-settings-button/reader-settings-button.component";
import { RouterService } from 'src/app/services/router.service';


@UntilDestroy()
@Component({
  selector: 'app-reader-page',
  imports: [CommonModule, FormsModule, BackButtonComponent, SwipeDirective, MinipagesCarouselComponent, ReaderSettingsButtonComponent],
  templateUrl: './reader-page.component.html'
})
export class ReaderPageComponent implements OnDestroy {
  comicId!: string;
  protected page!: number;
  route: ActivatedRoute = inject(ActivatedRoute);
  private readonly comicsService = inject(ComicsService);
  private readonly downloadService = inject(DownloadService);
  private readonly location = inject(Location);
  private readonly windowService = inject(WindowService);
  private readonly zoomService = inject(ZoomService);
  private readonly routerService = inject(RouterService);
  protected readonly readerSettingsService = inject(ReaderSettingsService);
  protected readonly orientationService = inject(OrientationService);
  protected readonly comic$: Observable<ComicsDatabase>;
  comic: ComicsDatabase | null = null;
  navigationDisplay: boolean = false;
  private debouncer: Subject<number> = new Subject();
  previousPageBlob: DoublePageBlob = undefined;
  nextPageBlob: DoublePageBlob = undefined;
  currentPageBlob: DoublePageBlob = undefined;
  lockNextPrevious: boolean = false;
  private incognito: boolean = false;

  private activatedDoublePage = false;
  readonly doublePage$: Observable<'single' | 'double'>;

  private rangePages: number[][] = [];

  constructor() {
    this.route.url.subscribe(segments => {
      const last = segments[segments.length - 1]?.path;
      this.incognito = last === 'incognito';
    });
    this.comic$ = this.route.params.pipe(
      map(params => params['id']),
      switchMap(id => {
        this.comicId = id;
        return this.comicsService.getComic(this.comicId);
      })
    );

    combineLatest([
      this.comic$,
      this.route.queryParams
    ]).pipe(
      untilDestroyed(this)
    ).subscribe(([comic, queryParams]) => {
      this.comic = comic;
      if (queryParams['page']) {
        this.page = Number(queryParams['page']);
      } else {
        this.page = comic.pageStatus;
      }
      this.loadPage();
    })

    this.debouncer.pipe(debounceTime(300)).pipe(untilDestroyed(this)).subscribe((page) => {
      this.page = page;
      this.loadPage();
    });

    this.orientationService.orientationChange$.pipe(
      filter(() => this.readerSettingsService.getValue('resetZoom')),
      untilDestroyed(this)
    ).subscribe(() => {
      //On pwa only reloading the page works
      //this.routerService.reloadCurrentRouteForce();
      this.resetZoomWithReload();
      //this.zoomService.resetZoom()
    });

    this.doublePage$ = combineLatest(
      this.readerSettingsService.doublePageConfiguration$,
      this.orientationService.orientationChange$.pipe(
        startWith(this.orientationService.getOrientation()),
      )
    ).pipe(
      map(([doublePageConfiguration, orientation]) => doublePageConfiguration === 'double' || orientation === 'landscape' && doublePageConfiguration === 'auto' ? 'double' : 'single'),
      tap(value => this.activatedDoublePage = value === 'double'),
      distinctUntilChanged()
    );

    combineLatest([
      this.comic$,
      this.doublePage$
    ]).pipe(untilDestroyed(this)).subscribe(([comic, doublePages]) => {
      this.rangePages = computeDoublePages(comic, doublePages === 'double');
      this.loadPage();
    })

    this.zoomService.setZoomEnabled(true);

  }

  ngOnDestroy(): void {
    this.zoomService.setZoomEnabled(false);
  }

  setPageImage() {
    if (this.currentPageBlob) {
      if (!this.currentPageBlob[1]) {
        const imageUrl = URL.createObjectURL(this.currentPageBlob[0]);
        const imgElement = document.getElementById('comicImage') as HTMLImageElement;
        imgElement.src = imageUrl;
      } else {
        // double page
        forkJoin([
          this.blobToImage$(this.currentPageBlob[0]),
          this.blobToImage$(this.currentPageBlob[1])
        ]).pipe(
          switchMap(([leftImg, rightImg]) => {
            const width = leftImg.width + rightImg.width;
            const height = Math.max(leftImg.height, rightImg.height);

            const canvas = document.createElement('canvas');
            canvas.width = width;
            canvas.height = height;
            const ctx = canvas.getContext('2d')!;

            ctx.drawImage(leftImg, 0, 0);
            ctx.drawImage(rightImg, leftImg.width, 0);

            // wrap toBlob in an observable
            return new Observable<Blob>((observer) => {
              canvas.toBlob((blob) => {
                if (blob) {
                  observer.next(blob);
                  observer.complete();
                } else {
                  observer.error('Canvas toBlob failed');
                }
              }, 'image/png');
            });
          })
        ).subscribe((blob: Blob) => {
          const imageUrl = URL.createObjectURL(blob);
          const imgElement = document.getElementById('comicImage') as HTMLImageElement;
          imgElement.src = imageUrl;
        });
      }
    }
  }

  private blobToImage$(blob: Blob): Observable<HTMLImageElement> {
    return new Observable((observer) => {
      const url = URL.createObjectURL(blob);
      const img = new Image();
      img.onload = () => {
        URL.revokeObjectURL(url);
        observer.next(img);
        observer.complete();
      };
      img.onerror = (err) => observer.error(err);
      img.src = url;
    });
  }


  getComicPage(page: number): Observable<DoublePageBlob> {
    this.lockNextPrevious = true;
    const pageCouple = this.rangePages.find(pair => pair.includes(page));
    if (pageCouple) {
      const page0$ = this.downloadService.getComicPage(this.comicId, pageCouple[0]);
      const page1$ = pageCouple.length > 1 ? this.downloadService.getComicPage(this.comicId, pageCouple[1]) : of(undefined);
      return forkJoin([page0$, page1$]).pipe(
        tap(() => this.lockNextPrevious = false)
      );
    }
    //SHOULD NEVER HAPPEN
    return EMPTY;
  }

  loadPage() {
    if (this.comicId && this.page != null) {
      this.previousPageBlob = undefined;
      this.nextPageBlob = undefined;
      this.setComicPage();
      this.getComicPage(this.page).pipe(untilDestroyed(this)).subscribe((response) => {
        this.currentPageBlob = response;
        this.setPageImage();
      });
      this.storeNextPage();
      this.storePreviousPage();
    }
  }

  storePreviousPage() {
    const decreasePage = this.decreasePage(this.page);
    if (decreasePage >= 0) {
      this.getComicPage(decreasePage).pipe(untilDestroyed(this)).subscribe((response) => this.previousPageBlob = response);
    };
  }

  storeNextPage() {
    const nextPage = this.increasePage(this.page);
    if (this.comic && nextPage < this.comic.pages) {
      this.getComicPage(nextPage).pipe(untilDestroyed(this)).subscribe((response) => this.nextPageBlob = response);
    }
  }

  setComicPage() {
    if (!this.incognito) {
      this.comicsService.setComicPageStatus(this.comicId, this.page).pipe(untilDestroyed(this)).subscribe();
    }
    const newParams = new URLSearchParams(window.location.search);
    newParams.set('page', String(this.page));
    // Push new query params without triggering Angular navigation
    this.location.replaceState(window.location.pathname, newParams.toString());
  }

  loadNextPage() {
    if (this.comicId && this.page != null) {
      this.setComicPage();
      this.previousPageBlob = this.currentPageBlob;
      this.currentPageBlob = this.nextPageBlob;
      this.setPageImage();
      this.storeNextPage();
    }
  }

  loadPreviousPage() {
    if (this.comicId && this.page != null) {
      this.setComicPage();
      this.nextPageBlob = this.currentPageBlob;
      this.currentPageBlob = this.previousPageBlob;
      this.setPageImage();
      this.storePreviousPage();
    }
  }


  increasePage(page: number): number {
    if (this.activatedDoublePage && this.comic) {
      if (this.comic.doublePages.includes(page + 1) || this.comic.doublePages.includes(page)) {
        return page + 1;
      } else if (page + 2 >= this.comic.pages) {
        return page + 1;
      } else {
        return page + 2;
      }
    }
    return page + 1;
  }

  nextPage(force: Boolean) {
    if (!this.lockNextPrevious) {
      if (force || (!this.windowService.isZoomed() && !this.navigationDisplay)) {
        if (this.comic?.pages === this.page + 1) {
          if (this.comic) {
            this.comic.readStatus = true;
          }
          this.comic.readStatus = true;
          if (!this.incognito) {
            this.comicsService.setComicListReadStatus([this.comic.id], true).pipe(untilDestroyed(this)).subscribe(() => this.backToDetails());
          } else {
            this.backToDetails();
          }
        } else {
          this.page = this.increasePage(this.page);
          this.loadNextPage();
        }
        resetRouteCache();
      }
    }
  }

  decreasePage(page: number): number {
    if (this.activatedDoublePage && this.comic) {
      if (this.comic.doublePages.includes(page - 1) || this.comic.doublePages.includes(page)) {
        return page - 1;
      } else if (page - 2 < 0) {
        return page - 1;
      } else {
        return page - 2;
      }
    }
    return page - 1;
  }

  previousPage(force: Boolean) {
    if (!this.lockNextPrevious) {
      if (force || (!this.windowService.isZoomed() && !this.navigationDisplay)) {
        if (this.page >= 1) {
          this.page = this.decreasePage(this.page);
          this.loadPreviousPage();
        }
        resetRouteCache();
      }
    }
  }

  toggleNavigation() {
    const lockZoom = this.windowService.isZoomed() && this.readerSettingsService.getValue('noMenuZoom');
    if (!lockZoom) {
      this.navigationDisplay = !this.navigationDisplay;
    }
  }

  backToDetails() {
    this.location.back();
  }

  onStepChange(event: number) {
    this.debouncer.next(event);
  }

  swipeNext() {
    if (this.readerSettingsService.getValue('gestures')) {
      this.nextPage(false);
    }
  }

  swipePrevious() {
    if (this.readerSettingsService.getValue('gestures')) {
      this.previousPage(false);
    }
  }

  resetZoomWithReload() {
    this.zoomService.setZoomEnabled(true);
    setTimeout(() => {
      window.location.replace(window.location.href);
    }, 50);
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === 'ArrowLeft') {
      this.previousPage(true);
    } else if (event.key === 'ArrowRight') {
      this.nextPage(true);
    } else if (event.key === 'Escape') {
      this.backToDetails();
    }
  }
}

type DoublePageBlob = [Blob, Blob | undefined] | undefined;

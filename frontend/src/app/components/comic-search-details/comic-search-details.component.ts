import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ComicsService, NotifierService } from 'services';
import { ComicSearchDetailsLinks, ComicsSearch, HttpResponseError, isHttpResponseError, Tag } from 'interfaces';
import { DownloadButtonComponent } from '../download-button/download-button.component';
import { BehaviorSubject, Observable, Subject, catchError, combineLatest, delay, distinct, distinctUntilChanged, filter, interval, map, of, share, shareReplay, startWith, switchMap, tap, withLatestFrom } from 'rxjs';
import { CommonModule } from '@angular/common';
import { HideRolesDirective } from 'directives';
import { Role } from 'interfaces';
import { distinctUntilValueChanged, notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { LoadingSpinnerPageComponent } from '../loading-spinner-page/loading-spinner-page.component';
import { ComicNotFoundComponent } from "../comic-not-found/comic-not-found.component";
import { Row, TwoColumnsTableComponent } from '../two-columns-table/two-columns-table.component';
import { fromCategoryToRow } from '../comic-database-details/comic-database-details.component';
import { WindowService } from 'services';
import { TagChipComponent } from "../tag-chip/tag-chip.component";
import { CarouselSeriesComicsComponent } from "../carousel-series-comics/carousel-series-comics.component";
import { GoToComicButtonComponent } from '../go-to-comic-button/go-to-comic-button.component';
import { MiniSpinnerComponent } from "../mini-spinner/mini-spinner.component";
import { ErrorPageComponent } from "../error-page/error-page.component";



@Component({
  selector: 'app-comic-search-details',
  imports: [DownloadButtonComponent, CommonModule, HideRolesDirective, LoadingSpinnerPageComponent, ComicNotFoundComponent, TwoColumnsTableComponent, TagChipComponent, CarouselSeriesComicsComponent, GoToComicButtonComponent, MiniSpinnerComponent, ErrorPageComponent],
  templateUrl: './comic-search-details.component.html'
})
@UntilDestroy()
export class ComicSearchDetailsComponent implements OnInit {
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly comicsService = inject(ComicsService);
  private readonly windowService = inject(WindowService);
  private readonly notifierService = inject(NotifierService);
  protected readonly comicSearchDetailsLinks$: Observable<ComicSearchDetailsLinks | undefined>;
  private comicSearchDetailsLinks: ComicSearchDetailsLinks | undefined = undefined;
  protected readonly comicsCarousel$: Observable<ComicsSearch[]>
  protected readonly rows$: Observable<Row[]>;
  protected readonly notFoundID$ = new Subject<string>;
  private idGc: string | null = null;
  protected tag: Tag | null = null;
  Role = Role;
  widthStyle: string = 'width: 0%';
  remainingComics: number | null = null;
  imageLoaded = false;
  private readonly triggerFetch$: Observable<void>;
  protected readonly scrollToIndex$: Subject<number> = new Subject<number>();
  protected errorMessage$ = new BehaviorSubject<HttpResponseError | null>(null);

  constructor() {

    const idGc$: Observable<string> = this.route.params.pipe(
      map(params => params['idGc']),
      tap(idGc => this.idGc = idGc),
      shareReplay({ bufferSize: 1, refCount: true }),
    );

    this.triggerFetch$ = this.comicsService.downloadingList$.pipe(
      filter(comics => comics.some(comic => comic.idGc === this.idGc) || this.comicSearchDetailsLinks?.downloadingStatus === 'downloading'),
      map(() => undefined),
    )

    //Trigger the fetch but wait 1 sec to avoid race conditions
    this.comicSearchDetailsLinks$ = combineLatest(
      [this.triggerFetch$.pipe(startWith(undefined)),
        idGc$]
    ).pipe(
      switchMap(() => this.comicsService.getComicSearchDetailsLinks(this.idGc ?? '')),
      catchError(catchError => {
        const error = catchError.error;
        if (isHttpResponseError(error) && (error.errorCode === 'SCRAPER_GATEWAY_ERROR' || error.errorCode === 'SCRAPER_PARSING_ERROR')) {
          this.notifierService.appendNotification({
            id: 0,
            title: 'Error',
            message: catchError.error.message as string,
            type: 'error'
          });
          this.errorMessage$.next(catchError.error);
          return of(undefined);
        } else {
          this.notFoundID$.next(this.idGc ?? '');
          return of(undefined);
        }
      }),
      shareReplay({ bufferSize: 1, refCount: true }),
    );

    this.comicsCarousel$ = this.comicSearchDetailsLinks$.pipe(
      tap(comicSearchDetailsLinks => this.comicSearchDetailsLinks = comicSearchDetailsLinks),
      filter(notNullOrUndefined()),
      map(comicSearchDetailsLinks => this.tag ?? comicSearchDetailsLinks.mainTag),
      filter(notNullOrUndefined()),
      distinctUntilValueChanged(),
      tap(tag => this.tag = tag),
      switchMap(mainTag => this.comicsService.tag(mainTag.link, 1, 1)),
      map(scrapperResponse => scrapperResponse.comicsSearchs),
      shareReplay({ bufferSize: 1, refCount: true }),
    );

    combineLatest([idGc$, this.comicsCarousel$]).pipe(
      untilDestroyed(this),
    ).subscribe(([idGc, comics]) => {
      for (let i = 0; i < comics.length; i++) {
        if (comics[i].idGc === idGc) {
          comics[i].highlight = true;
          // Give some time to start the scrolling animation
          setTimeout(() => {
            this.scrollToIndex$.next(i);
          }, 500);
        }
      }
    });



    interval(500).pipe(
      withLatestFrom(this.comicSearchDetailsLinks$),
      filter(([_, comicSearchDetailsLinks]) => comicSearchDetailsLinks?.downloadingStatus === 'downloading'),
      switchMap(() => this.comicsService.downloads()),
      map((downloads) => downloads.find(download => download.idGc === this.idGc)),
      filter(notNullOrUndefined()),
      untilDestroyed(this),
    ).subscribe((download) => {
      const progress = 100 * (download.currentBytes / download.totalBytes);
      this.widthStyle = `width: ${progress}%`;
      this.remainingComics = download.totalComics > 1 && !(download.currentComic === (download.totalComics - 1) && progress === 100) ? download.totalComics - download.currentComic : null;
    })

    this.rows$ = this.comicSearchDetailsLinks$.pipe(
      filter(notNullOrUndefined()),
      map(comic => [
        {
          title: 'Title',
          type: 'text',
          text: comic.title,
        },
        fromCategoryToRow(comic.category),
        {
          title: 'Year',
          type: 'text',
          text: comic.year,
        },
        {
          title: 'Size',
          type: 'text',
          text: comic.size,
        },
        {
          title: 'Link',
          type: 'link',
          link: comic.link,
        }
      ])
    );
  }

  ngOnInit(): void {
    this.windowService.scrollToTop(0, 'smooth');
  }
}

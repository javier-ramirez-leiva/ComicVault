import { Component, OnInit, inject } from '@angular/core';
import {
  ComicsService,
  TopBarService,
  PaginationRatioService,
  WindowService,
  NotifierService,
  ActivePageService,
  GridService,
} from 'services';
import {
  BehaviorSubject,
  EMPTY,
  Observable,
  Subject,
  catchError,
  combineLatest,
  debounceTime,
  delay,
  filter,
  map,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';
import { Category, ComicsSearch, HttpResponseError, isHttpResponseError } from 'interfaces';
import { ComicsSearchGridComponent } from '../comics-search-grid/comics-search-grid.component';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ViewportScroller } from '@angular/common';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { NoResultsComponent } from '../no-results/no-results.component';
import { LoadingSpinnerPageComponent } from '../loading-spinner-page/loading-spinner-page.component';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { MiniSpinnerComponent } from '../mini-spinner/mini-spinner.component';
import { ErrorPageComponent } from '../error-page/error-page.component';
import { ComicSearchTableComponent } from '../comic-search-table/comic-search-table.component';

@UntilDestroy()
@Component({
  selector: 'app-search-page',
  imports: [
    ComicsSearchGridComponent,
    CommonModule,
    NoResultsComponent,
    LoadingSpinnerPageComponent,
    MiniSpinnerComponent,
    ErrorPageComponent,
    ComicSearchTableComponent,
  ],
  templateUrl: './search-page.component.html',
})
export class SearchPageComponent implements OnInit {
  protected comics$ = new BehaviorSubject<ComicsSearch[] | undefined>(undefined);
  private comicsQuery$: Observable<ComicsSearch[]>;
  private scrollTrigger$ = new Subject<void>();
  protected readonly miniSpinner$ = new Subject<boolean>();
  page: number = 1;
  query: string | null = null;
  tag: string | null = null;
  category: Category | null = null;
  emptyResult$: Observable<boolean>;
  errorMessage$ = new BehaviorSubject<HttpResponseError | null>(null);
  viewportScroller: ViewportScroller = inject(ViewportScroller);
  errorOrEmpty: boolean = false;
  private readonly router: Router = inject(Router);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly topBarService = inject(TopBarService);
  private readonly comicsService = inject(ComicsService);
  private readonly paginationRatioService = inject(PaginationRatioService);
  private readonly windowService = inject(WindowService);
  private readonly notifierService = inject(NotifierService);
  private readonly activePageService = inject(ActivePageService);
  protected readonly gridService = inject(GridService);

  enableBottomEvents: boolean = false;

  constructor() {
    const pagesRatio = this.paginationRatioService.getPaginationRatio();

    this.comicsQuery$ = combineLatest([
      this.route.queryParams.pipe(
        tap((_) => {
          this.windowService.scrollToTop(0, 'smooth');
          this.comics$.next(undefined);
          this.page = 1;
          this.errorOrEmpty = false;
        }),
      ),
      this.scrollTrigger$.pipe(startWith(null)),
    ]).pipe(
      debounceTime(100),
      tap(() => (this.enableBottomEvents = false)),
      switchMap(([params, _]) => {
        this.errorMessage$.next(null);
        this.query = params['query'];
        this.category = params['category'];
        this.tag = params['tag'];
        let observable$;
        if (this.category) {
          observable$ = this.comicsService.trending(this.category, this.page, pagesRatio);
        } else if (this.query) {
          observable$ = this.comicsService.search(this.query, this.page, pagesRatio);
        } else if (this.tag) {
          observable$ = this.comicsService.tag(this.tag, this.page, pagesRatio);
        } else {
          this.category = 'all';
          observable$ = this.comicsService.trending(this.category, this.page, pagesRatio);
        }
        this.page++;
        return observable$.pipe(
          catchError((err) => {
            this.miniSpinner$.next(false);
            this.errorOrEmpty = true;
            const error = err.error;
            if (
              isHttpResponseError(error) &&
              (error.errorCode === 'SCRAPER_GATEWAY_ERROR' ||
                error.errorCode === 'SCRAPER_PARSING_ERROR')
            ) {
              this.notifierService.appendNotification({
                id: 0,
                title: 'Error',
                message: err.error.message as string,
                type: 'error',
              });
              this.errorMessage$.next(error);
            }
            return EMPTY;
          }),
        );
      }),
      tap(() => (this.enableBottomEvents = true)),
      map((scrapperResponse) => {
        this.errorOrEmpty =
          scrapperResponse.comicsSearchs.length === 0 || scrapperResponse.endReached;
        this.miniSpinner$.next(false);
        return scrapperResponse.comicsSearchs;
      }),
    );

    this.emptyResult$ = this.comics$.pipe(
      filter(notNullOrUndefined()),
      map((comics: ComicsSearch[]) => comics.length === 0),
    );

    this.activePageService.isDetailsPage$
      .pipe(delay(1000), untilDestroyed(this))
      .subscribe((isDetailPage) => (this.enableBottomEvents = !isDetailPage));
  }

  ngOnInit(): void {
    this.windowService.scrollBottom$
      .pipe(
        filter((_) => !this.errorOrEmpty),
        filter((_) => this.enableBottomEvents),
        untilDestroyed(this),
      )
      .subscribe(() => {
        this.miniSpinner$.next(true);
        this.scrollTrigger$.next();
      });

    this.comicsQuery$.pipe(untilDestroyed(this)).subscribe((comics: ComicsSearch[]) => {
      const currentComics = this.comics$.getValue();
      if (currentComics === undefined) {
        this.comics$.next(comics);
        return;
      } else {
        this.comics$.next([...currentComics, ...comics]);
      }
    });
  }
}

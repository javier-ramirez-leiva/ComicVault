import { Component, OnInit, inject, signal } from '@angular/core';
import {
  ComicsService,
  PaginationRatioService,
  WindowService,
  NotifierService,
  GridService,
} from 'services';
import {
  BehaviorSubject,
  EMPTY,
  Observable,
  catchError,
  filter,
  map,
  single,
  switchMap,
  tap,
} from 'rxjs';
import { Category, ComicsSearch, HttpResponseError, isHttpResponseError } from 'interfaces';
import { ComicsSearchGridComponent } from '../comics-search-grid/comics-search-grid.component';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { NoResultsComponent } from '../no-results/no-results.component';
import { LoadingSpinnerPageComponent } from '../loading-spinner-page/loading-spinner-page.component';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { ErrorPageComponent } from '../error-page/error-page.component';
import { ComicSearchTableComponent } from '../comic-search-table/comic-search-table.component';
import { PageNavigatorComponent } from '../page-navigator/page-navigator.component';

@UntilDestroy()
@Component({
  selector: 'app-search-page',
  imports: [
    ComicsSearchGridComponent,
    CommonModule,
    NoResultsComponent,
    LoadingSpinnerPageComponent,
    ErrorPageComponent,
    ComicSearchTableComponent,
    PageNavigatorComponent,
  ],
  templateUrl: './search-page.component.html',
})
export class SearchPageComponent implements OnInit {
  protected comics$ = new BehaviorSubject<ComicsSearch[] | undefined>(undefined);
  private comicsQuery$: Observable<ComicsSearch[]>;
  page: number = 1;
  query: string | null = null;
  tag: string | null = null;
  category: Category | null = null;
  emptyResult$: Observable<boolean>;
  errorMessage$ = new BehaviorSubject<HttpResponseError | null>(null);
  errorOrEmpty = signal(false);
  private readonly router: Router = inject(Router);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly comicsService = inject(ComicsService);
  private readonly paginationRatioService = inject(PaginationRatioService);
  private readonly windowService = inject(WindowService);
  private readonly notifierService = inject(NotifierService);
  protected readonly gridService = inject(GridService);

  constructor() {
    const pagesRatio = this.paginationRatioService.getPaginationRatio();

    this.comicsQuery$ = this.route.queryParams.pipe(
      tap((params) => {
        this.windowService.scrollToTop(0, 'smooth');
        this.comics$.next(undefined);
        this.page = params['page'] ?? 1;
        this.errorOrEmpty.set(false);
      }),
      switchMap((params) => {
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
        return observable$.pipe(
          catchError((err) => {
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
      map((scrapperResponse) => {
        this.errorOrEmpty.set(
          scrapperResponse.comicsSearchs.length === 0 || scrapperResponse.endReached,
        );
        return scrapperResponse.comicsSearchs;
      }),
    );

    this.emptyResult$ = this.comics$.pipe(
      filter(notNullOrUndefined()),
      map((comics: ComicsSearch[]) => comics.length === 0),
    );
  }

  ngOnInit(): void {
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

  onPageChange(page: number) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page },
      queryParamsHandling: 'merge',
    });
  }
}

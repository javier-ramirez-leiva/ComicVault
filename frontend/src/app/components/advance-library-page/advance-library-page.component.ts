import { Component, inject } from '@angular/core';
import { NoResultsComponent } from '../no-results/no-results.component';
import { ComicsDatabaseGridComponent } from '../comics-database-grid/comics-database-grid.component';
import { ComicDatabaseTableComponent } from '../comic-database-table/comic-database-table.component';
import { SeriesGridComponent } from '../series-grid/series-grid.component';
import { SeriesTableComponent } from '../series-table/series-table.component';
import { LoadingSpinnerPageComponent } from '../loading-spinner-page/loading-spinner-page.component';
import { CommonModule } from '@angular/common';
import { combineLatest, filter, map, Observable, startWith, switchMap } from 'rxjs';
import { ComicsDatabase, FilterComics, Series, FilterSeries } from 'interfaces';
import { ComicsService, GridService, MultiSelectService, TopBarService } from 'services';
import { AdvanceFilterFunnel } from '../advance-funnel-button/advance-funnel-button.component';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-advance-library-page',
  imports: [
    NoResultsComponent,
    ComicsDatabaseGridComponent,
    ComicDatabaseTableComponent,
    SeriesGridComponent,
    SeriesTableComponent,
    LoadingSpinnerPageComponent,
    CommonModule,
  ],
  templateUrl: './advance-library-page.component.html',
})
export class AdvanceLibraryPageComponent {
  filteredSeries$: Observable<Series[]>;
  filteredComics$: Observable<ComicsDatabase[]>;

  private readonly topBarService = inject(TopBarService);
  private readonly comicService = inject(ComicsService);
  private readonly multiSelectService = inject(MultiSelectService);
  private readonly route = inject(ActivatedRoute);
  protected readonly gridService = inject(GridService);

  contentSeries = false;

  constructor() {
    this.filteredSeries$ = combineLatest([
      this.topBarService.advanceFilterEvent$.pipe(startWith(null)),
      this.topBarService.searchTextChangeLibEvent$.pipe(startWith('')),
    ]).pipe(
      filter((_) => this.contentSeries),
      switchMap(([filter, search]) => {
        const series$ = filter
          ? this.comicService.allSeriesAdvanced(this.mapToFilterSeries(filter))
          : this.comicService.allSeries();
        return series$.pipe(
          map((series) => ({
            series,
            search,
          })),
        );
      }),
      map(({ series, search }) =>
        series.filter((serie) => serie.title.toLowerCase().includes(search.toLowerCase())),
      ),
    );
    this.filteredComics$ = combineLatest([
      this.topBarService.advanceFilterEvent$.pipe(startWith(null)),
      this.topBarService.searchTextChangeLibEvent$.pipe(startWith('')),
      this.multiSelectService.refresh$.pipe(startWith(null)),
    ]).pipe(
      filter((_) => !this.contentSeries),
      switchMap(([filter, search, _]) => {
        const comics$ = filter
          ? this.comicService.allComicsAdvanced(this.mapToFilterComics(filter))
          : this.comicService.allComics();
        return comics$.pipe(
          map((comics) => ({
            comics,
            search,
          })),
        );
      }),
      map(({ comics, search }) =>
        comics.filter((comic) => comic.title.toLowerCase().includes(search.toLowerCase())),
      ),
    );
  }

  ngOnInit(): void {
    this.route.url.subscribe((segments) => {
      const last = segments[segments.length - 1]?.path;
      this.contentSeries = last === 'series';
    });
  }

  private mapToFilterSeries(filter: AdvanceFilterFunnel): FilterSeries {
    return {
      sortAttribute: filter.seriesSortAttribute,
      sortDescendingDirection: filter.sortDescendingDirection,
      comicTitle: '',
      removeCategories: Object.entries(filter.categories)
        .filter(([_, value]) => !value)
        .map(([key, _]) => key),
      yearStart: filter.year.from || 0,
      yearEnd: filter.year.to || new Date().getFullYear(),
      issuesStart: filter.issues.from || 0,
      issuesEnd: filter.issues.to || Number.MAX_SAFE_INTEGER,
      modifiedAtStart: filter.modifiedAt.from || new Date(0),
      modifiedAtEnd: filter.modifiedAt.to || new Date(),
    };
  }

  private mapToFilterComics(filter: AdvanceFilterFunnel): FilterComics {
    return {
      sortAttribute: filter.comicSortAttribute,
      sortDescendingDirection: filter.sortDescendingDirection,
      comicTitle: '',
      notNotStartedUsers: filter.usersReadStatus
        .filter((user) => !user.notStarted)
        .map((user) => user.userName),
      notOnGoingUsers: filter.usersReadStatus
        .filter((user) => !user.ongoing)
        .map((user) => user.userName),
      notReadUsers: filter.usersReadStatus
        .filter((user) => !user.read)
        .map((user) => user.userName),
      removeCategories: Object.entries(filter.categories)
        .filter(([_, value]) => !value)
        .map(([key, _]) => key),
      yearStart: filter.year.from || 0,
      yearEnd: filter.year.to || new Date().getFullYear(),
      sizeStart: filter.size.from || 0,
      sizeEnd: filter.size.to || Number.MAX_SAFE_INTEGER,
      createdAtStart: filter.createdAt.from || new Date(0),
      createdAtEnd: filter.createdAt.to || new Date(),
    };
  }
}

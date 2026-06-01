import { Component, inject, OnInit } from '@angular/core';
import { ComicsDatabaseGridComponent } from '../comics-database-grid/comics-database-grid.component';
import { CommonModule } from '@angular/common';
import { SeriesGridComponent } from '../series-grid/series-grid.component';
import { GridService, TopBarService } from 'services';
import { Observable, combineLatest, map, startWith, switchMap, tap } from 'rxjs';
import { ComicsDatabase, Series } from 'interfaces';
import { ComicsService } from 'services';
import { FilterFunnel } from '../funnel-button/funnel-button.component';
import { MultiSelectService } from 'services';
import { NoResultsComponent } from '../no-results/no-results.component';
import { LoadingSpinnerPageComponent } from '../loading-spinner-page/loading-spinner-page.component';
import { ComicDatabaseTableComponent } from '../comic-database-table/comic-database-table.component';
import { SeriesTableComponent } from '../series-table/series-table.component';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-library-page',
  imports: [
    ComicsDatabaseGridComponent,
    CommonModule,
    SeriesGridComponent,
    NoResultsComponent,
    LoadingSpinnerPageComponent,
    ComicDatabaseTableComponent,
    SeriesTableComponent,
  ],
  templateUrl: './library-page.component.html',
})
export class LibraryPageComponent implements OnInit {
  contentSeries: boolean = false;
  filteredSeries$: Observable<Series[]>;
  filteredComics$: Observable<ComicsDatabase[]>;

  private readonly topBarService = inject(TopBarService);
  private readonly comicService = inject(ComicsService);
  private readonly multiSelectService = inject(MultiSelectService);
  protected readonly gridService = inject(GridService);
  private readonly route = inject(ActivatedRoute);

  constructor() {
    this.filteredSeries$ = combineLatest([
      this.comicService.allSeries(),
      this.topBarService.filterEvent$.pipe(),
      this.topBarService.searchTextChangeLibEvent$.pipe(startWith('')),
    ]).pipe(
      map(([series, filter, search]) =>
        this.sortSeries(
          this.filterSeries(series, filter, search),
          filter?.seriesSortAttribute ?? 'LATEST',
          filter?.sortDescendingDirection ?? false,
        ),
      ),
    );
    const allComics$ = this.multiSelectService.refresh$.pipe(
      startWith(null),
      switchMap(() => this.comicService.allComics()),
    );
    this.filteredComics$ = combineLatest([
      allComics$,
      this.topBarService.filterEvent$.pipe(),
      this.topBarService.searchTextChangeLibEvent$.pipe(startWith('')),
      this.multiSelectService.refresh$.pipe(startWith(null)),
    ]).pipe(
      map(([comics, filter, search, _]) =>
        this.sortComics(
          this.filterComics(comics, filter, search),
          filter?.comicSortAttribute ?? 'LATEST',
          filter?.sortDescendingDirection ?? false,
        ),
      ),
    );
  }

  ngOnInit(): void {
    this.route.url.subscribe((segments) => {
      const last = segments[segments.length - 1]?.path;
      this.contentSeries = last === 'series';
    });
  }

  sortSeries(series: Series[], sortAttribute: string, descending: boolean): Series[] {
    return series.sort((a, b) => {
      let compare = 0;
      switch (sortAttribute) {
        case 'LATEST':
          compare = new Date(b.modifiedAt).getTime() - new Date(a.modifiedAt).getTime();
          break;
        case 'TITLE':
          compare = a.title.localeCompare(b.title);
          break;
        case 'YEAR':
          const yearA = parseInt(a.year, 10);
          const yearB = parseInt(b.year, 10);
          const aValid = !isNaN(yearA);
          const bValid = !isNaN(yearB);

          if (!aValid && !bValid) {
            compare = 0;
          } else if (!aValid) {
            compare = 1; // a goes after b
          } else if (!bValid) {
            compare = -1; // b goes after a
          } else {
            compare = yearA - yearB;
          }
          break;
        case 'CATEGORY':
          compare = a.category.localeCompare(b.category);
          break;
        case 'ISSUES':
          compare = a.totalIssues - b.totalIssues;
          break;
      }
      return descending ? -compare : compare;
    });
  }

  sortComics(
    comics: ComicsDatabase[],
    sortAttribute: string,
    descending: boolean,
  ): ComicsDatabase[] {
    return comics.sort((a, b) => {
      let compare = 0;
      switch (sortAttribute) {
        case 'LATEST':
          compare = new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
          break;
        case 'TITLE':
          compare = a.title.localeCompare(b.title);
          break;
        case 'YEAR':
          const yearA = parseInt(a.year, 10);
          const yearB = parseInt(b.year, 10);
          const aValid = !isNaN(yearA);
          const bValid = !isNaN(yearB);

          if (!aValid && !bValid) {
            compare = 0;
          } else if (!aValid) {
            compare = 1; // a goes after b
          } else if (!bValid) {
            compare = -1; // b goes after a
          } else {
            compare = yearA - yearB;
          }
          break;
        case 'CATEGORY':
          compare = a.category.localeCompare(b.category);
          break;
        case 'SIZE':
          const sizeA = parseInt(a.size.replace(' MB', ''), 10);
          const sizeB = parseInt(b.size.replace(' MB', ''), 10);
          const aSizeValid = !isNaN(sizeA);
          const bSizeValid = !isNaN(sizeB);

          if (!aSizeValid && !bSizeValid) {
            compare = 0;
          } else if (!aSizeValid) {
            compare = 1; // a goes after b
          } else if (!bSizeValid) {
            compare = -1; // b goes after a
          } else {
            compare = sizeA - sizeB;
          }
          break;
        case 'READ STATUS':
          const getRank = (s: ComicsDatabase) => {
            if (s.readStatus) return 0;
            if (s.pageStatus > 0) return 1;
            return 2;
          };
          const rankCompare = getRank(a) - getRank(b);
          if (rankCompare !== 0) {
            compare = rankCompare;
          } else if (!a.readStatus) {
            const aProgress = a.pageStatus / a.pages;
            const bProgress = b.pageStatus / b.pages;
            compare = bProgress - aProgress; // most progressed first
          }
          break;
      }
      return descending ? -compare : compare;
    });
  }

  filterSeries(series: Series[], filter: FilterFunnel | null, search: string): Series[] {
    return series
      .filter((series) => {
        if (search === '') {
          return true;
        }
        return series.title.toLowerCase().includes(search.toLowerCase());
      })
      .filter((serie) => {
        if (filter === null) {
          return true;
        }
        switch (serie.category) {
          case 'dc':
            if (!filter.categories.dc) {
              return false;
            }
            break;
          case 'marvel':
            if (!filter.categories.marvel) {
              return false;
            }
            break;
          case 'other-comics':
            if (!filter.categories.other) {
              return false;
            }
            break;
        }
        if (serie.readStatus && !filter.readStatus.read) {
          return false;
        }
        if (serie.comics.some((comic) => comic.pageStatus > 0)) {
          if (!filter.readStatus.ongoing) {
            return false;
          }
        } else {
          if (!filter.readStatus.notStarted) {
            return false;
          }
        }
        if (filter.issues.from != '' && serie.totalIssues < filter.issues.from) {
          return false;
        }
        if (filter.issues.to != '' && serie.totalIssues > filter.issues.to) {
          return false;
        }
        if (filter.year.from != '' && parseInt(serie.year) < filter.year.from) {
          return false;
        }
        if (filter.year.to != '' && parseInt(serie.year) > filter.year.to) {
          return false;
        }
        return true;
      });
  }

  filterComics(
    comics: ComicsDatabase[],
    filter: FilterFunnel | null,
    search: string,
  ): ComicsDatabase[] {
    return comics
      .filter((comic) => {
        if (search === '') {
          return true;
        }
        return comic.title.toLowerCase().includes(search.toLowerCase());
      })
      .filter((comic) => {
        if (filter === null) {
          return true;
        }
        switch (comic.category) {
          case 'dc':
            if (!filter.categories.dc) {
              return false;
            }
            break;
          case 'marvel':
            if (!filter.categories.marvel) {
              return false;
            }
            break;
          case 'other-comics':
            if (!filter.categories.other) {
              return false;
            }
            break;
        }
        if (comic.readStatus && !filter.readStatus.read) {
          return false;
        }
        if (!comic.readStatus && comic.pageStatus > 0 && !filter.readStatus.ongoing) {
          return false;
        }
        if (!comic.readStatus && comic.pageStatus === 0 && !filter.readStatus.notStarted) {
          return false;
        }
        if (filter.size.from != '' && parseFloat(comic.size) < filter.size.from) {
          return false;
        }
        if (filter.size.to != '' && parseFloat(comic.size) > filter.size.to) {
          return false;
        }
        if (filter.year.from != '' && parseInt(comic.year) < filter.year.from) {
          return false;
        }
        if (filter.year.to != '' && parseInt(comic.year) > filter.year.to) {
          return false;
        }
        return true;
      });
  }
}

import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, shareReplay, Subject, tap } from 'rxjs';
import { LocalStorageService } from './local-storage.service';
import { ActivePageService } from './active-page.service';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@Injectable({
  providedIn: 'root',
})
@UntilDestroy()
export class GridService {
  private readonly localStorageService = inject(LocalStorageService);
  private readonly activePageService = inject(ActivePageService);

  gridOptions$ = new BehaviorSubject<GridOptions>({
    library: true,
    search: true,
    seriesDetails: true,
    downloads: true,
  });
  gridOptionActivePage$: Observable<boolean>;
  constructor() {
    const initialValue = this.localStorageService.getItem('gridOptions') ?? {
      library: true,
      search: true,
    };
    this.gridOptions$.next(initialValue);

    this.gridOptionActivePage$ = combineLatest([
      this.activePageService.activeRoot$,
      this.gridOptions$,
    ]).pipe(
      map(([activeRoot, gridOptions]) => {
        if (activeRoot === 'library') {
          return gridOptions.library;
        } else if (activeRoot === 'search') {
          return gridOptions.search;
        } else if (activeRoot === 'series') {
          return gridOptions.seriesDetails;
        } else {
          return gridOptions.downloads;
        }
      }),
      shareReplay({ bufferSize: 1, refCount: true }),
    );
  }

  toggleGrid() {
    const value = this.gridOptions$.value;
    const activeRoot = this.activePageService.activeRoot$.value;
    if (activeRoot === 'library') {
      value.library = !value.library;
    } else if (activeRoot === 'search') {
      value.search = !value.search;
    } else if (activeRoot === 'series') {
      value.seriesDetails = !value.seriesDetails;
    } else if (activeRoot === 'downloads') {
      value.downloads = !value.downloads;
    }
    this.localStorageService.setItem('gridOptions', value);
    this.gridOptions$.next(value);
  }
}

type GridOptions = {
  library: boolean;
  search: boolean;
  seriesDetails: boolean;
  downloads: boolean;
};

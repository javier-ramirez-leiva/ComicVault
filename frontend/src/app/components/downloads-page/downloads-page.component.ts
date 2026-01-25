import { Component, inject } from '@angular/core';
import { ComicsSearch } from 'interfaces';
import { Observable, combineLatest, interval, map, of, shareReplay, switchMap } from 'rxjs';
import { ComicsService, GridService } from 'services';
import { CommonModule } from '@angular/common';
import { ComicsDownloadGridComponent } from '../comics-download-grid/comics-download-grid.component';
import { ComicsDatabaseWithDownload, DownloadService } from 'services';
import { NoResultsComponent } from '../no-results/no-results.component';
import { ComicSearchTableComponent } from '../comic-search-table/comic-search-table.component';

@Component({
  selector: 'app-downloads-page',
  imports: [
    ComicsDownloadGridComponent,
    CommonModule,
    NoResultsComponent,
    ComicSearchTableComponent,
  ],
  templateUrl: './downloads-page.component.html',
})
export class DownloadsPageComponent {
  public comics$: Observable<ComicsSearch[]>;
  private readonly comicsService = inject(ComicsService);
  private readonly downloadsService = inject(DownloadService);
  protected readonly gridService = inject(GridService);
  protected emptyResult$: Observable<boolean>;
  protected localDownloads$: Observable<ComicsDatabaseWithDownload[]>;

  constructor() {
    this.comics$ = this.comicsService.downloadingList$.pipe(
      //
      shareReplay({ bufferSize: 1, refCount: true }),
    );
    this.localDownloads$ = interval(500).pipe(
      //
      switchMap(() => this.downloadsService.getLocalDownload()),
      shareReplay({ bufferSize: 1, refCount: true }),
    );

    this.emptyResult$ = combineLatest([this.comics$, this.localDownloads$]).pipe(
      map(([comics, localDownloads]) => comics.length === 0 && localDownloads.length === 0),
    );
  }
}

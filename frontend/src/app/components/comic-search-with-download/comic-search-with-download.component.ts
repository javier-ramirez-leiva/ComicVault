import { Component, Input, OnInit, WritableSignal, inject, signal } from '@angular/core';
import { CoverCardComponent } from '../cover-card/cover-card.component';
import { ComicsSearch } from 'interfaces';
import { Observable, filter, map } from 'rxjs';

import { ComicsService } from 'services';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';

@Component({
  selector: 'app-comic-search-with-download',
  imports: [CoverCardComponent],
  templateUrl: './comic-search-with-download.component.html',
})
export class ComicSearchWithDownloadComponent implements OnInit {
  @Input({ required: true }) public comic!: ComicsSearch;
  private readonly comicsService = inject(ComicsService);

  protected progress$ = new Observable<number>();
  url: string[] = [];

  constructor() {
    this.progress$ = this.comicsService.downloadingList$.pipe(
      map((downloads) => downloads.find((download) => download.idGc === this.comic.idGc)),
      filter(notNullOrUndefined()),
      map((download) => 100 * (download.currentBytes / download.totalBytes)),
    );
  }

  ngOnInit(): void {
    this.url = ['/comics-search', this.comic.idGc, 'details'];
  }
}

import { Component, Input, OnInit } from '@angular/core';
import { BehaviorSubject, Observable, from, map, tap } from 'rxjs';
import { ComicsSearch } from 'interfaces';

import { ComicSearchWithDownloadComponent } from '../comic-search-with-download/comic-search-with-download.component';

@Component({
  selector: 'app-comics-download-grid',
  imports: [ComicSearchWithDownloadComponent],
  templateUrl: './comics-download-grid.component.html',
})
export class ComicsDownloadGridComponent {
  @Input() public comics!: ComicsSearch[];

  trackByComic(index: number, comic: ComicsSearch): string {
    return comic.idGc;
  }
}

import { Component, Input, inject } from '@angular/core';
import { ComicsSearch } from 'interfaces';
import { Observable, tap } from 'rxjs';
import { ComicSearchComponent } from '../comic-search/comic-search.component';


@Component({
  selector: 'app-comics-search-grid',
  imports: [ComicSearchComponent],
  templateUrl: './comics-search-grid.component.html',
})
export class ComicsSearchGridComponent {
  @Input({ required: true }) public comics!: ComicsSearch[];
  @Input() public trackNames: Boolean = true;

  trackByComic(index: number, comic: ComicsSearch): string {
    return comic.idGc;
  }
}

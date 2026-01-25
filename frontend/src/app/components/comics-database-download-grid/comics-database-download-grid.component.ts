import { Component, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { ComicsDatabase } from 'interfaces';
import { CommonModule } from '@angular/common';
import { ComicsDatabaseWithDownloadComponent } from '../comics-database-with-download/comics-database-with-download.component';
import { ComicsDatabaseWithDownload } from 'services';

@Component({
  selector: 'app-comics-database-download-grid',
  imports: [CommonModule, ComicsDatabaseWithDownloadComponent],
  templateUrl: './comics-database-download-grid.component.html',
})
export class ComicsDatabaseDownloadGridComponent {
  @Input() public localDownloads$!: Observable<ComicsDatabaseWithDownload[]>;

  trackByComic(index: number, localDownload: ComicsDatabaseWithDownload): string {
    return localDownload.comic.id;
  }
}

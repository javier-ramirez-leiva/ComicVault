import { Component, Input, inject } from '@angular/core';
import { ComicsService } from 'services';
import { ComicsDatabase } from 'interfaces';
import { NotifierService } from 'services';
import { DownloadSeriesButtonComponent } from '../download-series-button/download-series-button.component';
import { DownloadService } from 'services';

@Component({
  selector: 'app-download-file-button',
  imports: [],
  templateUrl: './download-file-button.component.html'
})
export class DownloadFileButtonComponent {
  @Input() comic!: ComicsDatabase | null;
  private readonly downloadService = inject(DownloadService);
  private readonly notifier = inject(NotifierService);


  downloadFile(): void {
    if (this.comic) {
      this.notifier.appendNotification({
        id: 0,
        title: 'Download on device launched',
        message: this.comic.title,
        type: 'info'
      });
      this.downloadService.downloadComic(this.comic);
    }
  }
}

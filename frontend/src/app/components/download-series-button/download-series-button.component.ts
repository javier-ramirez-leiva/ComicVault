import { Component, Input, inject } from '@angular/core';
import { Series } from 'interfaces';
import { ComicsService } from 'services';
import { NotifierService } from 'services';
import { DownloadService } from 'services';

@Component({
  selector: 'app-download-series-button',
  imports: [],
  templateUrl: './download-series-button.component.html'
})
export class DownloadSeriesButtonComponent {
  @Input() series!: Series | null;
  private readonly downloadService = inject(DownloadService);
  private readonly notifier = inject(NotifierService);


  downloadSeries(): void {
    if (this.series) {
      this.notifier.appendNotification({
        id: 0,
        title: 'Download on device launched',
        message: this.series.title,
        type: 'info'
      });
      const comics = this.series.comics;
      this.downloadService.downloadComicList(comics);
    }
  }
}

import { Component, Input, inject } from '@angular/core';
import { ComicsService, NotifierService } from 'services';

import { RouterService } from 'src/app/services/router.service';

@Component({
  selector: 'app-scan-lib-button',
  imports: [],
  templateUrl: './scan-lib-button.component.html',
})
export class ScanLibButtonComponent {
  @Input({ required: true }) mini: boolean = false;

  private readonly comicsService = inject(ComicsService);
  private readonly notifierService = inject(NotifierService);
  private readonly routerService = inject(RouterService);

  scanLib(): void {
    this.notifierService.appendProcessingNotification({
      id: 0,
      message: 'Scanning...',
      type: 'scan',
    });
    this.comicsService.scanLib().subscribe((response) => {
      if (response) {
        this.notifierService.appendNotification({
          id: 0,
          title: 'Success',
          message: 'Comics refreshed!',
          type: 'success',
        });
        this.routerService.reloadCurrentRoute();
      } else {
        this.notifierService.appendNotification({
          id: 0,
          title: 'In progress',
          message: 'Scan was already in progress',
          type: 'success',
        });
      }
    });
  }
}

import { Component, Input, inject } from '@angular/core';
import { ComicsService } from 'services';
import { ModalService } from 'services';
import { ModalMessageComponent } from '../modal-message/modal-message.component';
import { NotifierService } from 'services';
import { filter, switchMap } from 'rxjs';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { CommonModule } from '@angular/common';
import { RouterService } from 'src/app/services/router.service';

@Component({
  selector: 'app-clean-lib-button',
  imports: [CommonModule],
  templateUrl: './clean-lib-button.component.html',
})
export class CleanLibButtonComponent {
  @Input({ required: true }) mini: boolean = false;

  private readonly comicsService = inject(ComicsService);
  private readonly modalService = inject(ModalService);
  private readonly notifierService = inject(NotifierService);
  private readonly routerService = inject(RouterService);

  triggerCleanDialog() {
    const message = 'Are you sure you want to delete metadata on non existing comics?';
    this.modalService
      .open<boolean, { message: string | undefined }>(ModalMessageComponent, { message: message })
      .pipe(
        filter((response) => response === true),
        switchMap((_) => {
          this.notifierService.appendProcessingNotification({
            id: 0,
            message: 'Cleaning...',
            type: 'clean',
          });
          return this.comicsService.cleanLibrary();
        }),
      )
      .subscribe((response) => {
        if (response) {
          this.notifierService.appendNotification({
            id: 0,
            title: 'Cleaned',
            message: 'Library cleaned!',
            type: 'warning',
          });
          this.routerService.reloadCurrentRoute();
        } else {
          this.notifierService.appendNotification({
            id: 0,
            title: 'In progress',
            message: 'Clean was already in progress',
            type: 'warning',
          });
        }
      });
  }
}

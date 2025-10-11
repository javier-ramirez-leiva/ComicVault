import { Component, Input, inject } from '@angular/core';
import { ComicsService } from 'services';
import { ModalService } from 'services';
import { NotifierService } from 'services';
import { filter, switchMap } from 'rxjs';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { CommonModule } from '@angular/common';
import { ModalDeleteLibraryComponent } from '../modal-delete-library/modal-delete-library.component';
import { DeleteReadOptions } from 'interfaces';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { RouterService } from 'src/app/services/router.service';

@Component({
  selector: 'app-delete-lib-button',
  imports: [CommonModule],
  templateUrl: './delete-lib-button.component.html'
})
export class DeleteLibButtonComponent {
  @Input({ required: true }) mini: boolean = false;

  private readonly comicsService = inject(ComicsService);
  private readonly modalService = inject(ModalService);
  private readonly notifierService = inject(NotifierService);
  private readonly routerService = inject(RouterService);

  triggerDeleteDialog() {
    const message = "Are you sure you want to delete all read comics?";
    this.modalService.open<DeleteReadOptions, {}>(ModalDeleteLibraryComponent, {}).pipe(
      filter(notNullOrUndefined()),
      switchMap(deleteReadOption => {
        this.notifierService.appendProcessingNotification({
          id: 0,
          message: 'Deleting...',
          type: 'clean'
        })
        return this.comicsService.deleteReadComics(deleteReadOption);
      }),
    ).subscribe(response => {
      if (response) {
        this.notifierService.appendNotification({
          id: 0,
          title: 'Deleted',
          message: 'Comics read deleted!',
          type: 'warning'
        });
        this.routerService.reloadCurrentRoute();
      } else {
        this.notifierService.appendNotification({
          id: 0,
          title: 'In progress',
          message: 'Delete was already in progress',
          type: 'warning'
        });
      }

    });
  }
}

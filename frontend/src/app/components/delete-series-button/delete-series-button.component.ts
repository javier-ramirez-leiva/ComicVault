import { Component, Input, inject } from '@angular/core';
import { ComicsService } from 'services';
import { NotifierService } from 'services';
import { Router } from '@angular/router';
import { ActivePageService } from 'services';
import { ModalService } from 'services';
import { ComicsDatabase, Series } from 'interfaces';
import { ModalMessageComponent } from '../modal-message/modal-message.component';
import { filter, switchMap } from 'rxjs';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';

@Component({
  selector: 'app-delete-series-button',
  imports: [],
  templateUrl: './delete-series-button.component.html',
})
@UntilDestroy()
export class DeleteSeriesButtonComponent {
  private readonly comicsService: ComicsService = inject(ComicsService);
  private readonly notifierService: NotifierService = inject(NotifierService);
  private readonly router: Router = inject(Router);
  private readonly activePageService: ActivePageService = inject(ActivePageService);
  private readonly modalService = inject(ModalService);
  displayDialog: boolean = false;

  @Input() series!: Series | null;

  triggerModal() {
    const message = `Are you sure you want to delete ${this.series?.title} with ${this.series?.comics.length} comics?`;
    this.modalService
      .open<boolean, { message: string | undefined }>(ModalMessageComponent, { message: message })
      .pipe(
        filter((response) => response === true),
        switchMap((_) => {
          return this.comicsService.deleteComicList(
            this.series!.comics.map((comic: ComicsDatabase) => comic.id),
          );
        }),
        untilDestroyed(this),
      )
      .subscribe((response) => {
        this.notifierService.appendNotification({
          id: 0,
          title: 'Series deleted',
          message: this.series!.title,
          type: 'warning',
        });
        resetRouteCache();

        this.router.navigate(['/' + this.activePageService.activePage$.getValue()]);
      });
  }
}

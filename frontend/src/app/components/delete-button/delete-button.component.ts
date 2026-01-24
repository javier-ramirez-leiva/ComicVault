import { Component, Input, inject } from '@angular/core';
import { ComicsDatabase } from 'interfaces';
import { ComicsService } from 'services';
import { NotifierService } from 'services';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ActivePageService } from 'services';
import { ModalService } from 'services';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ModalMessageComponent } from '../modal-message/modal-message.component';
import { filter, switchMap } from 'rxjs';
import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';

@Component({
  selector: 'app-delete-button',
  imports: [CommonModule],
  templateUrl: './delete-button.component.html',
})
@UntilDestroy()
export class DeleteButtonComponent {
  private readonly comicsService: ComicsService = inject(ComicsService);
  private readonly notifierService: NotifierService = inject(NotifierService);
  private readonly router: Router = inject(Router);
  private readonly activePageService: ActivePageService = inject(ActivePageService);
  private readonly modalService = inject(ModalService);
  displayDialog: boolean = false;

  @Input() comic!: ComicsDatabase | null;

  triggerModal() {
    const message = `Are you sure you want to delete ${this.comic?.title}`;
    this.modalService
      .open<boolean, { message: string | undefined }>(ModalMessageComponent, { message: message })
      .pipe(
        filter((response) => response === true),
        switchMap((_) => {
          return this.comicsService.deleteComic(this.comic!.id);
        }),
        untilDestroyed(this),
      )
      .subscribe((response) => {
        this.notifierService.appendNotification({
          id: 0,
          title: 'Comic deleted',
          message: this.comic!.title,
          type: 'warning',
        });
        resetRouteCache();
        if (response.hasOwnProperty('nextComicID') && response['nextComicID'] !== '') {
          this.router.navigate(['/comics', response['nextComicID'], 'details']);
        } else {
          this.router.navigate(['/' + this.activePageService.activePage$.getValue()]);
        }
      });
  }
}

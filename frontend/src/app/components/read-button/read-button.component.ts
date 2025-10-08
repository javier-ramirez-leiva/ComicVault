import { Component, Input, inject } from '@angular/core';
import { ComicsDatabase } from 'interfaces';
import { ComicsService, ModalService } from 'services';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ModalReadComponent, ReadOption } from '../modal-read/modal-read.component';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { filter } from 'rxjs';


@Component({
  selector: 'app-read-button',
  imports: [CommonModule],
  templateUrl: './read-button.component.html'
})
@UntilDestroy()
export class ReadButtonComponent {
  private readonly comicsService: ComicsService = inject(ComicsService);
  private readonly modalService: ModalService = inject(ModalService);
  router: Router = inject(Router);

  @Input({ required: true }) comic!: ComicsDatabase;

  displayReadModal() {
    this.modalService.open<ReadOption, undefined>(ModalReadComponent, undefined).pipe(
      filter(notNullOrUndefined()),
      untilDestroyed(this)
    ).subscribe(response => {
      switch (response) {
        case 'read':
          this.read();
          break;
        case 'incognito':
          this.readIncongnito();
          break;
        case 'beginning':
          this.startFromTheBeginning();
          break;
      }
    })
  }

  read() {
    const routerLink = ['/comics', this.comic.id, 'read', 'standard'];
    this.router.navigate(routerLink);
  }

  readIncongnito() {
    const routerLink = ['/comics', this.comic.id, 'read', 'incognito'];
    this.router.navigate(routerLink);
  }

  startFromTheBeginning() {
    this.comicsService.setComicPageStatus(this.comic?.id, 0).pipe(
      untilDestroyed(this)
    ).subscribe(() => this.read());
  }
}

import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { ModalService } from 'services';
import { Exception } from 'src/app/interfaces/exception';
import { ModalExceptionComponent } from '../modal-exception/modal-exception.component';
import { filter } from 'rxjs';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 'app-exceptions-table',
  imports: [CommonModule],
  templateUrl: './exceptions-table.component.html',
})
export class ExceptionsTableComponent {
  @Input({ required: true }) exceptions!: Exception[];

  private readonly modalService = inject(ModalService);

  trackExceptions(index: number, exceptions: Exception): string {
    return exceptions.timeStamp.toString();
  }

  displayExceptionDetails(exception: Exception) {
    this.modalService
      .open<null, { exception: Exception | undefined }>(ModalExceptionComponent, {
        exception: exception,
      })
      .pipe(
        filter((response) => response === true),
        untilDestroyed(this),
      )
      .subscribe();
  }
}

import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { ModalService } from 'services';
import { Exception } from 'src/app/interfaces/exception';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { Router } from '@angular/router';

@UntilDestroy()
@Component({
  selector: 'app-exceptions-table',
  imports: [CommonModule],
  templateUrl: './exceptions-table.component.html',
})
export class ExceptionsTableComponent {
  @Input({ required: true }) exceptions!: Exception[];
  @Input({ required: false }) displayDetails: boolean = true;

  private readonly modalService = inject(ModalService);
  private readonly router = inject(Router);

  trackExceptions(index: number, exceptions: Exception): string {
    return exceptions.timeStamp.toString();
  }

  navigateToExceptionDetails(exception: Exception) {
    this.router.navigate([`/settings/exception/${exception.exceptionId}/details`]);
  }
}

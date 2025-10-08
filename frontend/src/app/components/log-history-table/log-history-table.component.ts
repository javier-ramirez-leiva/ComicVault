import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { Log } from 'interfaces';
import { ModalService } from 'services';
import { ModalLogComponent } from '../modal-log/modal-log.component';
import { filter } from 'rxjs';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { RouterModule } from '@angular/router';

@UntilDestroy()
@Component({
  selector: 'app-log-history-table',
  imports: [CommonModule, RouterModule],
  templateUrl: './log-history-table.component.html'
})
export class LogHistoryTableComponent {
  @Input({ required: true }) logs!: Log[];
  @Input({ required: false }) displayJobId = false;

  private readonly modalService = inject(ModalService);

  trackLogs(index: number, log: Log): string {
    return log.timeStamp.toString();
  }

  displayDetails(log: Log) {
    this.modalService.open<null, { log: Log | undefined }>(ModalLogComponent, { log: log }).pipe(
      filter(response => response === true),
      untilDestroyed(this)
    ).subscribe(response => {
    });
  }

}

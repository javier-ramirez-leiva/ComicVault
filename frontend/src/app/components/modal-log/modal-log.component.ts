import { Component, inject, Input } from '@angular/core';
import { ModalComponent } from 'interfaces';
import { Log, isSeverrity } from 'interfaces';
import { CommonModule } from '@angular/common';
import { Row, TwoColumnsTableComponent } from '../two-columns-table/two-columns-table.component';
import { formatDate } from '../../utils/dates';
import { ModalDetailsTopComponent } from '../modal-details-top/modal-details-top.component';
import { filter, map, Observable } from 'rxjs';
import { ComicsService } from 'services';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-modal-log',
  imports: [CommonModule, TwoColumnsTableComponent, ModalDetailsTopComponent],
  templateUrl: './modal-log.component.html',
})
export class ModalLogComponent implements ModalComponent<null, { log: Log | undefined }> {
  @Input({ required: true }) data?: { log: Log | undefined };
  close!: (response?: null) => void;
  protected rows: Row[] = [];

  private readonly router = inject(Router);

  protected comicUrl: string | undefined = undefined;
  protected seriesUrl: string | undefined = undefined;

  ngOnInit(): void {
    if (this.data) {
      this.rows = [
        {
          title: 'Severity',
          type: 'chip',
          text: this.data?.log?.severityMessage ?? '',
          severity:
            this.data?.log?.severity && isSeverrity(this.data.log.severity)
              ? this.data.log.severity
              : 'Desactivated',
        },
        {
          title: 'Message',
          type: 'text',
          text: this.data.log?.message ?? '',
        },
        {
          title: 'Timestamp',
          type: 'text',
          text: this.data.log?.timeStamp ? formatDate(this.data.log.timeStamp) : '',
        },
        {
          title: 'Username',
          type: 'text',
          text: this.data.log?.username ?? '',
        },
        {
          title: 'Job Id',
          type: 'text',
          text: this.data.log?.jobId.toString() ?? '',
        },
        {
          title: 'Details',
          type: 'text',
          text: this.data?.log?.details ?? '',
        },
      ];

      if (this.data.log?.comicId) {
        this.comicUrl = `/comics/${this.data.log.comicId}/details`;
      }

      if (this.data.log?.seriesId) {
        this.seriesUrl = `/series/${this.data.log.seriesId}/details`;
      }
    }
  }

  goTo(url: string) {
    this.router.navigateByUrl(url);
  }
}

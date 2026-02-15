import { Component, Input, OnInit } from '@angular/core';
import { ModalComponent } from 'interfaces';
import { Exception } from 'src/app/interfaces/exception';
import { Row, TwoColumnsTableComponent } from '../two-columns-table/two-columns-table.component';
import { formatDate } from 'src/app/utils/dates';
import { ModalDetailsTopComponent } from '../modal-details-top/modal-details-top.component';

@Component({
  selector: 'app-modal-exception',
  imports: [ModalDetailsTopComponent, TwoColumnsTableComponent],
  templateUrl: './modal-exception.component.html',
})
export class ModalExceptionComponent
  implements OnInit, ModalComponent<null, { exception: Exception | undefined }>
{
  @Input({ required: true }) data?: { exception: Exception | undefined };
  close!: (response?: null) => void;

  protected rows: Row[] = [];

  ngOnInit(): void {
    if (this.data) {
      this.rows = [
        {
          title: 'Type',
          type: 'text',
          text: this.data.exception?.type ?? '',
        },
        {
          title: 'Timestamp',
          type: 'text',
          text: this.data.exception?.timeStamp ? formatDate(this.data.exception.timeStamp) : '',
        },
        {
          title: 'Message',
          type: 'text',
          text: this.data.exception?.message ?? '',
        },
        {
          title: 'Details',
          type: 'text',
          text: this.data?.exception?.details.join('\n') ?? '',
        },
      ];
    }
  }
}

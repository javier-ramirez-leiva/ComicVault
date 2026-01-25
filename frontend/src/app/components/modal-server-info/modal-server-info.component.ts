import { Component, Input, OnInit } from '@angular/core';
import { ModalComponent, ServerInfo } from 'interfaces';
import { Row, TwoColumnsTableComponent } from '../two-columns-table/two-columns-table.component';
import { ModalDetailsTopComponent } from '../modal-details-top/modal-details-top.component';
import { ModalDetailsBottomComponent } from '../modal-details-bottom/modal-details-bottom.component';

@Component({
  selector: 'app-modal-server-info',
  imports: [ModalDetailsTopComponent, TwoColumnsTableComponent, ModalDetailsBottomComponent],
  templateUrl: './modal-server-info.component.html',
})
export class ModalServerInfoComponent
  implements ModalComponent<null, { serverInfo: ServerInfo | undefined }>, OnInit
{
  @Input({ required: true }) data?: { serverInfo: ServerInfo | undefined };
  close!: (response?: null) => void;

  protected rows: Row[] = [];

  ngOnInit(): void {
    if (this.data) {
      this.rows = [
        {
          title: 'Api version',
          type: 'text',
          text: this.data.serverInfo?.apiVersion ?? '',
        },
        {
          title: 'Schema version',
          type: 'text',
          text: this.data.serverInfo?.schemaVersion ?? '',
        },
        {
          title: 'Commit branch',
          type: 'text',
          text: this.data.serverInfo?.branch ?? '',
        },
        {
          title: 'Commit id',
          type: 'text',
          text: this.data.serverInfo?.commitId ?? '',
        },
        {
          title: 'Commit message',
          type: 'text',
          text: this.data.serverInfo?.commitMessage ?? '',
        },
        {
          title: 'Commit time',
          type: 'text',
          text: this.data.serverInfo?.commitTime ?? '',
        },
      ];
    }
  }
}

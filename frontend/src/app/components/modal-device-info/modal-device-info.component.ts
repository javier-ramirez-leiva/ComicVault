import { Component, Input, OnInit } from '@angular/core';
import { ModalComponent } from 'interfaces';
import { DeviceInfo } from 'interfaces';

import { Row, TwoColumnsTableComponent } from '../two-columns-table/two-columns-table.component';
import { ModalDetailsTopComponent } from '../modal-details-top/modal-details-top.component';
import { ModalDetailsBottomComponent } from '../modal-details-bottom/modal-details-bottom.component';
import { formatDate } from 'src/app/utils/dates';

@Component({
  selector: 'app-modal-device-info',
  imports: [TwoColumnsTableComponent, ModalDetailsTopComponent, ModalDetailsBottomComponent],
  templateUrl: './modal-device-info.component.html',
})
export class ModalDeviceInfoComponent
  implements ModalComponent<null, { device: DeviceInfo | undefined }>, OnInit
{
  @Input({ required: true }) data?: { device: DeviceInfo | undefined };
  close!: (response?: null) => void;

  protected rows: Row[] = [];

  ngOnInit(): void {
    if (this.data) {
      this.rows = [
        {
          title: 'Username',
          type: 'text',
          text: this.data.device?.username ?? '',
        },
        {
          title: 'Device',
          type: 'text',
          text: this.data.device?.device ?? '',
        },
        {
          title: 'OS',
          type: 'text',
          text: this.data.device?.os ?? '',
        },
        {
          title: 'IP and date of login',
          type: 'text',
          text: `${this.data.device?.ip ?? ''} - ${this.data.device?.createdAt ? formatDate(this.data.device?.createdAt) : ''}`,
        },
        {
          title: 'Browser',
          type: 'text',
          text: this.data.device?.browser ?? '',
        },
        {
          title: 'Orientation',
          type: 'text',
          text: this.data.device?.orientation ?? '',
        },
        {
          title: 'User Agent',
          type: 'text',
          text: this.data.device?.userAgent ?? '',
        },
      ];
    }
  }
}

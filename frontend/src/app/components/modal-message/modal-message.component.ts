import { Component, Input } from '@angular/core';
import { ModalComponent } from 'interfaces';
import { CrossModalComponent } from '../cross-modal/cross-modal.component';

@Component({
  selector: 'app-modal-message',
  imports: [CrossModalComponent],
  templateUrl: './modal-message.component.html',
})
export class ModalMessageComponent implements ModalComponent<
  boolean,
  { message: string | undefined }
> {
  @Input({ required: true }) data?: { message: string | undefined };

  close!: (response?: boolean) => void;

  confirm(value: boolean) {
    this.close(value);
  }

  cancel() {
    this.close(undefined);
  }
}

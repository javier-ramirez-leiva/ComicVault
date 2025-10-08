import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ModalComponent } from 'interfaces';
import { CrossModalComponent } from "../cross-modal/cross-modal.component";

@Component({
  selector: 'app-modal-message-list',
  imports: [CommonModule, CrossModalComponent],
  templateUrl: './modal-message-list.component.html',
})
export class ModalMessageListComponent implements ModalComponent<boolean, { message: string, list: string[] }> {
  @Input({ required: true }) data?: { message: string, list: string[] };

  close!: (response?: boolean) => void;

  confirm(value: boolean) {
    this.close(value);
  }

  cancel() {
    this.close(undefined);
  }
}

import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ModalComponent } from 'interfaces';
import { CrossModalComponent } from '../cross-modal/cross-modal.component';

@Component({
  selector: 'app-modal-help',
  imports: [CommonModule, CrossModalComponent],
  templateUrl: './modal-help.component.html',
})
export class ModalHelpComponent implements ModalComponent<undefined, { message: string }> {
  @Input({ required: true }) data?: { message: string };

  close!: (response?: boolean) => void;

  cancel() {
    this.close(undefined);
  }
}

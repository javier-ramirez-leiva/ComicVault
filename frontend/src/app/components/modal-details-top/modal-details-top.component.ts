import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CrossModalComponent } from '../cross-modal/cross-modal.component';

@Component({
  selector: 'app-modal-details-top',
  imports: [CrossModalComponent],
  templateUrl: './modal-details-top.component.html',
})
export class ModalDetailsTopComponent {
  @Input({ required: true }) title!: string;
  @Output() closeModal = new EventEmitter<void>();

  close() {
    this.closeModal.emit();
  }
}

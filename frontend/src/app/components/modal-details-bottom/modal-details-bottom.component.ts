import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-modal-details-bottom',
  imports: [],
  templateUrl: './modal-details-bottom.component.html',
})
export class ModalDetailsBottomComponent {
  @Output() closeModal = new EventEmitter<void>();

  close() {
    this.closeModal.emit();
  }
}

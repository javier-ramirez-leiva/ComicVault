import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-cross-modal',
  imports: [],
  templateUrl: './cross-modal.component.html'
})
export class CrossModalComponent {
  @Output() onCloseClick: EventEmitter<void> = new EventEmitter<void>();

  closeModal() {
    this.onCloseClick.emit();
  }
}

import { Component } from '@angular/core';
import { ModalComponent } from 'interfaces';
import { RadioButtonInputComponent } from '../radio-button-input/radio-button-input.component';
import { CrossModalComponent } from '../cross-modal/cross-modal.component';

@Component({
  selector: 'app-modal-read',
  imports: [RadioButtonInputComponent, CrossModalComponent],
  templateUrl: './modal-read.component.html',
})
export class ModalReadComponent implements ModalComponent<ReadOption, undefined> {
  close!: (response?: ReadOption) => void;

  confirm(readOption: ReadOption) {
    this.close(readOption);
  }

  cancel() {
    this.close(undefined);
  }
}

export type ReadOption = 'read' | 'incognito' | 'beginning';

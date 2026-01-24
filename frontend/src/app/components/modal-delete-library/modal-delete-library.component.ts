import { Component } from '@angular/core';
import { CrossModalComponent } from '../cross-modal/cross-modal.component';
import { RadioButtonInputComponent } from '../radio-button-input/radio-button-input.component';
import { DeleteReadOptions, ModalComponent } from 'interfaces';

@Component({
  selector: 'app-modal-delete-library',
  imports: [CrossModalComponent, RadioButtonInputComponent],
  templateUrl: './modal-delete-library.component.html',
})
export class ModalDeleteLibraryComponent implements ModalComponent<DeleteReadOptions, {}> {
  private deleteReadOptions: DeleteReadOptions = 'READ_BY_ALL';

  close!: (response?: DeleteReadOptions) => void;

  confirm() {
    this.close(this.deleteReadOptions);
  }

  cancel() {
    this.close(undefined);
  }

  updateResponse(deleteReadOptions: DeleteReadOptions) {
    this.deleteReadOptions = deleteReadOptions;
  }
}

import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-edit-metadata-button',
  imports: [],
  templateUrl: './edit-metadata-button.component.html',
})
export class EditMetadataButtonComponent {
  @Output() onEdit = new EventEmitter<void>();
  @Output() onSave = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  protected editStatus = false;

  click() {
    this.editStatus = !this.editStatus;
    this.onEdit.emit();
  }

  ok() {
    this.editStatus = !this.editStatus;
    this.onSave.emit();
  }

  cancel() {
    this.editStatus = !this.editStatus;
    this.onCancel.emit();
  }
}

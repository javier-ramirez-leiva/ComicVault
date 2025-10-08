import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-boolean-checkbox',
  imports: [CommonModule],
  templateUrl: './boolean-checkbox.component.html',
})
export class BooleanCheckboxComponent {
  @Input({ required: true }) value!: boolean;
  @Input({ required: false }) label!: string;
  @Output() valueChange = new EventEmitter<boolean>();

  onToggleChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.value = input.checked;
    this.valueChange.emit(this.value);
  }
}

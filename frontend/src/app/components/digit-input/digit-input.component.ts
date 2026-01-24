import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-digit-input',
  imports: [CommonModule, FormsModule],
  templateUrl: './digit-input.component.html',
})
export class DigitInputComponent {
  @Input({ required: true }) value: number = 0;
  @Output() valueChange = new EventEmitter<number>();

  onNgModelChange(value: any) {
    this.value = Number(value); // convert to number safely
    this.valueChange.emit(this.value);
  }
}

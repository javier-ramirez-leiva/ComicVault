
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-input-color',
  imports: [FormsModule],
  templateUrl: './input-color.component.html',
})
export class InputColorComponent {
  @Input({ required: true }) value: string = '';
  @Output() valueChange = new EventEmitter<string>();

  onValueChange(value: string) {
    this.valueChange.emit(value);
  }
}

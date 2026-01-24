
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-boolean-slider',
  imports: [FormsModule],
  templateUrl: './boolean-slider.component.html',
})
export class BooleanSliderComponent {
  @Input({ required: true }) value!: boolean;
  @Input({ required: false }) label!: string;
  @Output() valueChange = new EventEmitter<boolean>();

  onToggleChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.value = input.checked;
    this.valueChange.emit(this.value);
  }
}

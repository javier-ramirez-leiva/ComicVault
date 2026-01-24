import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-two-choices-chip',
  imports: [CommonModule],
  templateUrl: './two-choices-chip.component.html',
})
export class TwoChoicesChipComponent {
  @Input({ required: false }) value: boolean = false;
  @Input({ required: true }) trueChipLabel!: string;
  @Input({ required: true }) falseChipLabel!: string;
  @Output() valueChange = new EventEmitter<boolean>();

  emitValue(value: boolean) {
    this.value = value;
    this.valueChange.emit(value);
  }
}

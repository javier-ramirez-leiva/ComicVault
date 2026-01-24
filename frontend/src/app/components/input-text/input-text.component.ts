import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HelpButtonComponent } from '../help-button/help-button.component';

@Component({
  selector: 'app-input-text',
  imports: [FormsModule, HelpButtonComponent],
  templateUrl: './input-text.component.html',
})
export class InputTextComponent {
  @Input({ required: false }) title: string = '';
  @Input({ required: true }) value: string = '';
  @Input({ required: true }) id: string = '';
  @Input({ required: false }) disabled: boolean = false;
  @Input({ required: false }) password: boolean = false;
  @Input({ required: false }) helpMessage: string | undefined = undefined;
  @Output() valueChange = new EventEmitter<string>();

  onValueChange(value: string) {
    this.valueChange.emit(value);
  }
}

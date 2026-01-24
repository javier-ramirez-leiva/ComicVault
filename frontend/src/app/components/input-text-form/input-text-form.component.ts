import { Component, Input, forwardRef } from '@angular/core';
import { FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-input-text-form',
  imports: [FormsModule, CommonModule],
  templateUrl: './input-text-form.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputTextFormComponent),
      multi: true,
    },
  ],
})
export class InputTextFormComponent {
  @Input({ required: false }) title: string = '';
  @Input({ required: false }) multiLine = false;

  value = '';
  isDisabled = false;

  private onChange = (_: any) => {};
  protected onTouched = () => {};

  // Called by Angular when external model changes
  writeValue(value: any): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
  }

  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.value = input.value;
    this.onChange(this.value);
  }
}

import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, ViewChild, forwardRef } from '@angular/core';
import { FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-date-text-form',
  imports: [FormsModule, CommonModule],
  templateUrl: './date-text-form.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DateTextFormComponent),
      multi: true,
    },
  ],
})
export class DateTextFormComponent {
  @Input({ required: false }) title: string = '';

  value = '';
  isDisabled = false;

  @ViewChild('input', { static: false }) input!: ElementRef<HTMLInputElement>;

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

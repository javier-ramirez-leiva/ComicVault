import { CommonModule } from '@angular/common';
import { Component, Input, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';


@Component({
  selector: 'app-boolean-slider-form',
  imports: [CommonModule],
  templateUrl: './boolean-slider-form.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => BooleanSliderFormComponent),
      multi: true,
    }
  ]
})
export class BooleanSliderFormComponent implements ControlValueAccessor {
  @Input({ required: false }) label!: string;

  value = false;
  isDisabled = false;

  private onChange = (_: any) => { };
  protected onTouched = () => { };

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
    this.value = input.checked;
    this.onChange(this.value);
  }
}

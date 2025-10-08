import { Component, ElementRef, ViewChild, forwardRef, viewChild } from '@angular/core';
import { ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-digit-input-form',
  imports: [FormsModule],
  templateUrl: './digit-input-form.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DigitInputFormComponent),
      multi: true,
    }
  ]
})
export class DigitInputFormComponent implements ControlValueAccessor {
  value = 0;
  isDisabled = false;

  @ViewChild('input', { static: false }) input!: ElementRef<HTMLInputElement>;

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
    this.value = Number(input.value);
    this.onChange(this.value);
  }

  increaseValue(): void {
    const inputEl = this.input.nativeElement;
    this.value = Number(inputEl.value || 0);
    this.value++;
    inputEl.value = this.value.toString();
    this.onChange(this.value);
  }

  decreaseValue(): void {
    const inputEl = this.input.nativeElement;
    this.value = Number(inputEl.value || 0);
    this.value--;
    inputEl.value = this.value.toString();
    this.onChange(this.value);
  }

}

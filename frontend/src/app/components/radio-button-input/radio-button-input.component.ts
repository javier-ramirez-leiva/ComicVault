import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';


@Component({
  selector: 'app-radio-button-input',
  imports: [],
  templateUrl: './radio-button-input.component.html'
})
export class RadioButtonInputComponent implements AfterViewInit {
  @Input({ required: true }) label: string = '';
  @Input({ required: true }) value!: any;
  @Input({ required: false }) initValue!: any;
  @Output() valueChange = new EventEmitter<any>();
  @ViewChild('radioBtn') radioBtn!: ElementRef<HTMLInputElement>;


  onValueChange() {
    this.valueChange.emit(this.value);
  }

  select() {
    this.radioBtn.nativeElement.checked = true;
    this.radioBtn.nativeElement.dispatchEvent(new Event('click'));
  }

  ngAfterViewInit(): void {
    if (this.value === this.initValue) {
      this.radioBtn.nativeElement.checked = true;
      this.radioBtn.nativeElement.dispatchEvent(new Event('click'));
    }
  }
}

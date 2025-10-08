import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-expander-button',
  imports: [],
  templateUrl: './expander-button.component.html'
})
export class ExpanderButtonComponent {
  @Input({ required: true }) title!: string;
  @Output() buttonClicked = new EventEmitter<void>();

  expanded = false;
  onButtonClick(): void {
    this.expanded = !this.expanded;
    this.buttonClicked.emit();
  }
}

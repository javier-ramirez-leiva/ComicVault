import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, signal } from '@angular/core';

@Component({
  selector: 'app-expander-button',
  imports: [CommonModule],
  templateUrl: './expander-button.component.html',
})
export class ExpanderButtonComponent {
  @Input({ required: true }) title!: string;
  @Input({ required: false }) upperBorder = false;
  @Input({ required: false }) bottomBorder = false;
  @Output() buttonClicked = new EventEmitter<void>();

  @Input() settingsStyle = false;
  @Input() open: boolean | undefined;
  @Input() description = '';
  @Input() panelId: string | undefined;

  expanded = signal(false);
  isExpanded(): boolean {
    return this.open ?? this.expanded();
  }
  onButtonClick(): void {
    if (this.open === undefined) {
      this.expanded.update((value) => !value);
    }
    this.buttonClicked.emit();
  }
}

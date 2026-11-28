import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-right-panel-funnel-button',
  imports: [],
  templateUrl: './right-panel-funnel-button.component.html',
})
export class RightPanelFunnelButtonComponent {
  @Input() expanded = false;
  @Input() panelId = '';

  @Output() funnelButtonClicked = new EventEmitter<void>();

  onFunnelButtonClick() {
    this.funnelButtonClicked.emit();
  }
}

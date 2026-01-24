import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-right-panel-funnel-button',
  imports: [],
  templateUrl: './right-panel-funnel-button.component.html',
})
export class RightPanelFunnelButtonComponent {
  @Output() funnelButtonClicked = new EventEmitter<void>();

  onFunnelButtonClick() {
    this.funnelButtonClicked.emit();
  }
}

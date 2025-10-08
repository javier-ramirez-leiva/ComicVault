import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-right-panel-title',
  imports: [],
  templateUrl: './right-panel-title.component.html'
})
export class RightPanelTitleComponent {
  @Input({ required: true }) title: string = '';
  @Output() onCloseClick: EventEmitter<void> = new EventEmitter<void>();

  onCloseDrawer(): void {
    this.onCloseClick.emit();
  }

}

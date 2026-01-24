import { Component, EventEmitter, Input, Output } from '@angular/core';
import { BottomDrawerButtonComponent } from '../bottom-drawer-button/bottom-drawer-button.component';

@Component({
  selector: 'app-bottom-drawer-buttons',
  imports: [BottomDrawerButtonComponent],
  templateUrl: './bottom-drawer-buttons.component.html',
})
export class BottomDrawerButtonsComponent {
  @Input({ required: true }) open!: boolean;
  @Output() onDrawerOpen = new EventEmitter<boolean>();

  onDrawerOpenClick() {
    this.onDrawerOpen.emit(!this.open);
  }
}

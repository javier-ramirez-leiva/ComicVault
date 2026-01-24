import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-avatar-user',
  imports: [CommonModule],
  templateUrl: './avatar-user.component.html',
})
export class AvatarUserComponent {
  @Input({ required: true }) username!: string;
  @Input({ required: true }) color!: string;
  @Input({ required: true }) size!: 'small' | 'large';
  @Output() onClick = new EventEmitter<void>();

  onClickHandler() {
    this.onClick.emit();
  }
}

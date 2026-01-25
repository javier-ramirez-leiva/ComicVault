import { Component, Input, inject } from '@angular/core';
import { Notifications } from 'interfaces';
import { NotificationComponent } from '../notification/notification.component';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-notification-grid',
  imports: [CommonModule, NotificationComponent],
  templateUrl: './notification-grid.component.html',
})
export class NotificationGridComponent {
  @Input() public notifications$!: Observable<Notifications[]>;
}

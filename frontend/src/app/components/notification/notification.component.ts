import { Component, Input } from '@angular/core';
import { Notifications } from 'interfaces';
import { NotifierService } from 'services';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-notification',
  imports: [CommonModule],
  templateUrl: './notification.component.html'
})
export class NotificationComponent {
  @Input() notification!: Notifications;

  constructor(private notificationService: NotifierService) { }

  ngOnInit(): void {
    setTimeout(() => {
      this.notificationService.removeNotification(this.notification.id);
    }, 3000);// Clear the notification after 3 seconds
  }
}


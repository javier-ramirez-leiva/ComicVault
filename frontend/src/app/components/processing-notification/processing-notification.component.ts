import { Component, Input } from '@angular/core';
import { ProcessingNotifications } from 'interfaces';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-processing-notification',
  imports: [CommonModule],
  templateUrl: './processing-notification.component.html'
})
export class ProcessingNotificationComponent {
  @Input() processingNotification!: ProcessingNotifications;
}

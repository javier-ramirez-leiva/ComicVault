import { Component, Input } from '@angular/core';
import { ProcessingNotifications } from 'interfaces';


@Component({
  selector: 'app-processing-notification',
  imports: [],
  templateUrl: './processing-notification.component.html',
})
export class ProcessingNotificationComponent {
  @Input() processingNotification!: ProcessingNotifications;
}

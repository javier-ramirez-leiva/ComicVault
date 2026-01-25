import { Component, Input } from '@angular/core';
import { ProcessingNotifications } from 'interfaces';
import { ProcessingNotificationComponent } from '../processing-notification/processing-notification.component';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-processing-notification-grid',
  imports: [ProcessingNotificationComponent, CommonModule],
  templateUrl: './processing-notification-grid.component.html',
})
export class ProcessingNotificationGridComponent {
  @Input() public processingNotifications$!: Observable<ProcessingNotifications[]>;
}

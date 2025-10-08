import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Notifications, ProcessingNotifications } from 'interfaces';


@Injectable({
  providedIn: 'root'
})
export class NotifierService {
  private notifications: Notifications[] = [];
  private notificationsSubject = new Subject<Notifications[]>();
  private notifications$ = this.notificationsSubject.asObservable();
  private processingNotifications: ProcessingNotifications[] = [];
  private processingNotificationsSubject = new Subject<ProcessingNotifications[]>();
  private processingNotifications$ = this.processingNotificationsSubject.asObservable();
  private index: number = 1;


  constructor() { }

  appendNotification(notification: Notifications) {
    this.removeProcessingNotification(this.index);
    notification.id = this.index;
    this.index++;
    this.notifications.push(notification);
    this.notificationsSubject.next(this.notifications);
  }

  removeNotification(id: number) {
    this.notifications = this.notifications.filter(notification => notification.id !== id);
    this.notificationsSubject.next(this.notifications);
  }

  getNotifications$() {
    return this.notifications$;
  }


  appendProcessingNotification(processingNotification: ProcessingNotifications) {
    processingNotification.id = this.index;
    this.processingNotifications.push(processingNotification);
    this.processingNotificationsSubject.next(this.processingNotifications);
    setTimeout(() => {
      this.removeProcessingNotification(processingNotification.id);
    }, 2000);// Clear the notification after 2 seconds
  }

  removeProcessingNotification(id: number) {
    this.processingNotifications = this.processingNotifications.filter(processingNotification => processingNotification.id !== id);
    this.processingNotificationsSubject.next(this.processingNotifications);
  }

  getProcessingNotifications$() {
    return this.processingNotifications$;
  }
}

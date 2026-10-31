import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Notifications, ProcessingNotifications } from 'interfaces';

@Injectable({
  providedIn: 'root',
})
export class NotifierService {
  private notifications: Notifications[] = [];
  /** Keeps the current notifications available to a grid created after an emission. */
  private readonly notificationsSubject = new BehaviorSubject<Notifications[]>([]);
  private readonly notifications$ = this.notificationsSubject.asObservable();
  private processingNotifications: ProcessingNotifications[] = [];
  /** Processing notifications need the same route-independent delivery guarantee. */
  private readonly processingNotificationsSubject = new BehaviorSubject<ProcessingNotifications[]>([]);
  private readonly processingNotifications$ = this.processingNotificationsSubject.asObservable();
  private index: number = 1;
  private processingNotificationIndex: number = 1;

  constructor() {}

  appendNotification(notification: Notifications): void {
    const notificationWithId = { ...notification, id: this.index };
    this.index++;
    this.notifications = [...this.notifications, notificationWithId];
    this.notificationsSubject.next([...this.notifications]);
  }

  removeNotification(id: number): void {
    this.notifications = this.notifications.filter((notification) => notification.id !== id);
    this.notificationsSubject.next([...this.notifications]);
  }

  getNotifications$(): Observable<Notifications[]> {
    return this.notifications$;
  }

  appendProcessingNotification(processingNotification: ProcessingNotifications): void {
    const notificationWithId = {
      ...processingNotification,
      id: this.processingNotificationIndex++,
    };
    this.processingNotifications = [...this.processingNotifications, notificationWithId];
    this.processingNotificationsSubject.next([...this.processingNotifications]);
    setTimeout(() => {
      this.removeProcessingNotification(notificationWithId.id);
    }, 2000); // Clear the notification after 2 seconds
  }

  removeProcessingNotification(id: number): void {
    this.processingNotifications = this.processingNotifications.filter(
      (processingNotification) => processingNotification.id !== id,
    );
    this.processingNotificationsSubject.next([...this.processingNotifications]);
  }

  getProcessingNotifications$(): Observable<ProcessingNotifications[]> {
    return this.processingNotifications$;
  }
}

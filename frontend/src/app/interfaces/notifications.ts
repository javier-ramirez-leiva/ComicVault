export interface Notifications {
  id: number;
  title: string;
  message: string;
  type: 'error' | 'success' | 'info' | 'warning' | 'debug';
}

export interface ProcessingNotifications {
  id: number;
  message: string;
  type: 'warning' | 'clean' | 'scan' | 'download' | 'empty';
}

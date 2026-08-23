export interface Exception {
  exceptionId: string;
  type: string;
  timeStamp: string;
  message: string;
  username: string;
  endpoint: string;
  details: string[];
}

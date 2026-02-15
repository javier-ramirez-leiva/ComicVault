export interface Exception {
  jobId: string;
  type: string;
  timeStamp: string;
  timeStampFomatted: string;
  message: string;
  details: string[];
}

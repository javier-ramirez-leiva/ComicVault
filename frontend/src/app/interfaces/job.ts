export type JobType =
  | 'DOWNLOAD'
  | 'DOWNLOAD_LIST'
  | 'DELETE'
  | 'SCAN_LIB'
  | 'CLEAN_LIB'
  | 'DELETE_LIB';

export type JobStatus = 'ON_GOING' | 'COMPLETED' | 'ERROR';

export interface Job {
  jobId: string;
  timeStamp: string;
  timeStampFomatted: string;
  duration: string;
  username: string;
  logsIds: number[];
  type: JobType;
  status: JobStatus;
}

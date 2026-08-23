export type JobType =
  | 'DOWNLOAD'
  | 'DOWNLOAD_LIST'
  | 'DELETE'
  | 'SCAN_LIB'
  | 'CLEAN_LIB'
  | 'DELETE_LIB';

export type JobStatus = 'ON_GOING' | 'COMPLETED' | 'ERROR' | 'INTERRUPTED';
export interface Job {
  jobId: string;
  timeStamp: string;
  duration: number;
  username: string;
  logsIds: number[];
  type: JobType;
  status: JobStatus;
}

export type JobSortAttribute = 'LATEST' | 'STATUS' | 'TYPE' | 'DURATION' | 'USERNAME';
export interface JobFilterTypes {
  download: boolean;
  downloadList: boolean;
  delete: boolean;
  scanLib: boolean;
  cleanLib: boolean;
  deleteLib: boolean;
}

export interface JobFilterStatus {
  onGoing: boolean;
  completed: boolean;
  error: boolean;
  interrupted: boolean;
}

export interface JobFilter {
  jobSortAttribute: JobSortAttribute;
  sortDescendingDirection: boolean;
  username: string | null;
  types: JobFilterTypes;
  status: JobFilterStatus;
  dateStart: Date | undefined;
  dateEnd: Date | undefined;
}

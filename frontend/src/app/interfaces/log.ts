const Severities = ['Error', 'Warning', 'Info'] as const;

export type Severity = (typeof Severities)[number];

export interface Log {
  timeStamp: string;
  severity: Severity;
  severityMessage: string;
  message: string;
  messageHref: boolean;
  details: string;
  username: string;
  comicId: string;
  seriesId: string;
  jobId: number;
}

export type LogSortAttribute = 'LATEST' | 'SEVERITY' | 'USERNAME';

export interface Severities {
  info: boolean;
  error: boolean;
  warning: boolean;
}

export interface LogFilter {
  logSortAttribute: LogSortAttribute;
  sortDescendingDirection: boolean;
  username: string | null;
  messageContains: string | null;
  detailsContains: string | null;
  severities: Severities;
  dateStart: Date | undefined;
  dateEnd: Date | undefined;
}

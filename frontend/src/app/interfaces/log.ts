const Severities = ['Success', 'Error', 'Warning', 'Info', 'Desactivated'] as const;

export type Severity = (typeof Severities)[number];

export function isSeverrity(severity: string): severity is Severity {
  return ['Success', 'Error', 'Warning', 'Info', 'Desactivated'].includes(severity);
}

export interface Log {
  timeStamp: string;
  timeStampFomatted: string;
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

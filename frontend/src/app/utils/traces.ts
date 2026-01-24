import { environment } from 'src/environments/environment';

export function consoleLog(message?: any, ...optionalParams: any[]): void {
  if (environment.logTraceEnabled) {
    console.log(message, optionalParams);
  }
}

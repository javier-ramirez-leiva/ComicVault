import { Injectable, inject } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs';
import { Log, LogFilter } from 'interfaces';

@Injectable({
  providedIn: 'root',
})
export class LogsService {
  private readonly httpService = inject(HttpService);

  getHistoryLogs(page: number, filter: LogFilter | undefined): Observable<Log[]> {
    let url = `/logs?page=${page}`;
    return this.httpService.request<Log[]>('POST', url, filter);
  }

  getLogsForJobId(id: number): Observable<Log[]> {
    return this.httpService.request<Log[]>('GET', `/logs/search?jobId=${id}`);
  }
}

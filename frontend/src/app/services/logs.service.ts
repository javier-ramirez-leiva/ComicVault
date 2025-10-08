import { Injectable, inject } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs';
import { Log } from 'interfaces';

@Injectable({
  providedIn: 'root'
})
export class LogsService {

  private readonly httpService = inject(HttpService);

  getHistoryLogs(page: number): Observable<Log[]> {
    return this.httpService.request<Log[]>(
      'GET',
      `/logs?page=${page}`,
    );
  }
  getLogsForJobId(id: number): Observable<Log[]> {
    return this.httpService.request<Log[]>(
      'GET',
      `/logs/search?jobId=${id}`,
    );
  }
}

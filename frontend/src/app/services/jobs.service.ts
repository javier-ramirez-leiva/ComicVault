import { inject, Injectable } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs';
import { Job, JobFilter } from 'interfaces';

@Injectable({
  providedIn: 'root',
})
export class JobsService {
  private readonly httpService = inject(HttpService);

  getJobs(page: number, filter: JobFilter | undefined): Observable<Job[]> {
    let url = `/jobs?page=${page}`;
    return this.httpService.request<Job[]>('POST', url, filter);
  }

  getJob(jobId: number): Observable<Job> {
    return this.httpService.request<Job>('GET', `/job/search?jobId=${jobId}`);
  }
}

import { inject, Injectable } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs';
import { Job } from 'interfaces';

@Injectable({
  providedIn: 'root'
})
export class JobsService {

  private readonly httpService = inject(HttpService);

  getJobs(page: number): Observable<Job[]> {
    return this.httpService.request<Job[]>(
      'GET',
      `/jobs?page=${page}`,
    );
  }

  getJob(jobId: number): Observable<Job> {
    return this.httpService.request<Job>(
      'GET',
      `/job/search?jobId=${jobId}`,
    );
  }
}

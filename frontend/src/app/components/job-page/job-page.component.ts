import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Job, Log } from 'interfaces';
import { map, Observable, shareReplay, switchMap } from 'rxjs';
import { JobsTableComponent } from '../jobs-table/jobs-table.component';
import { LogHistoryTableComponent } from '../log-history-table/log-history-table.component';
import { JobsService, LogsService } from 'services';

@Component({
  selector: 'app-job-page',
  imports: [CommonModule, JobsTableComponent, LogHistoryTableComponent],
  templateUrl: './job-page.component.html',
})
export class JobPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly jobsService = inject(JobsService);
  private readonly logsService = inject(LogsService);

  protected readonly job$: Observable<Job>;
  protected readonly logs$: Observable<Log[]>;

  constructor() {
    const idGc$: Observable<string> = this.route.params.pipe(
      map((params) => params['id']),
      shareReplay({ bufferSize: 1, refCount: true }),
    );

    this.job$ = idGc$.pipe(switchMap((jobId) => this.jobsService.getJob(Number(jobId))));

    this.logs$ = idGc$.pipe(switchMap((jobId) => this.logsService.getLogsForJobId(Number(jobId))));
  }
}

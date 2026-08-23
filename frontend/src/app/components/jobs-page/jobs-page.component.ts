import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Job, JobFilter } from 'interfaces';
import { filter, Observable, switchMap } from 'rxjs';
import { JobsService, TopBarService } from 'services';
import { PageNavigatorComponent } from '../page-navigator/page-navigator.component';
import { CommonModule } from '@angular/common';
import { JobsTableComponent } from '../jobs-table/jobs-table.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 'app-jobs-page',
  imports: [PageNavigatorComponent, CommonModule, JobsTableComponent],
  templateUrl: './jobs-page.component.html',
})
export class JobsPageComponent implements OnInit {
  private readonly jobsService = inject(JobsService);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly router: Router = inject(Router);
  private readonly topBarService = inject(TopBarService);

  jobs$: Observable<Job[]>;
  page: number = 1;
  filter: JobFilter | undefined;

  constructor() {
    this.jobs$ = this.route.queryParams.pipe(
      filter((params) => !!params['page'] || !!params['filter']),
      switchMap((params) => {
        this.page = params['page'] ? params['page'] : 1;
        this.filter = params['filter'] ? JSON.parse(params['filter']) : undefined;
        return this.jobsService.getJobs(this.page, this.filter);
      }),
    );
  }

  ngOnInit() {
    this.topBarService.jobFilterEvent$.pipe(untilDestroyed(this)).subscribe((filter) => {
      this.router.navigate(['settings', 'jobs'], {
        queryParams: { filter: JSON.stringify(filter) },
      });
    });
  }

  onPageChange(page: number) {
    this.page = page;
    this.navigate();
  }

  private navigate() {
    this.router.navigate(['/settings/jobs'], {
      queryParams: { page: this.page, filter: JSON.stringify(this.filter) },
    });
  }
}

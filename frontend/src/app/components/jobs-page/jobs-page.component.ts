import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Job } from 'interfaces';
import { Observable, switchMap, tap } from 'rxjs';
import { JobsService } from 'services';
import { PageNavigatorComponent } from "../page-navigator/page-navigator.component";
import { CommonModule } from '@angular/common';
import { JobsTableComponent } from "../jobs-table/jobs-table.component";

@Component({
  selector: 'app-jobs-page',
  imports: [PageNavigatorComponent, CommonModule, JobsTableComponent],
  templateUrl: './jobs-page.component.html',
})
export class JobsPageComponent {
  private readonly jobsService = inject(JobsService);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly router: Router = inject(Router);

  jobs$: Observable<Job[]>;
  page: number = 1;

  constructor() {
    this.jobs$ = this.route.queryParams.pipe(
      switchMap((params) => {
        this.page = params['page'] ? params['page'] : 1;
        return this.jobsService.getJobs(this.page);
      })
    );
  }

  onPageChange(page: number) {
    this.page = page;
    this.navigate();
  }

  private navigate() {
    this.router.navigate(['/settings/jobs'], { queryParams: { page: this.page } });
  }
}

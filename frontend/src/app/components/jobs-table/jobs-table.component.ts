import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Job, JobStatus, JobType } from 'interfaces';
import { ModalService } from 'services';
import { RouterService } from 'src/app/services/router.service';
import { CdkDropList } from "@angular/cdk/drag-drop";

@Component({
  selector: 'app-jobs-table',
  imports: [CommonModule],
  templateUrl: './jobs-table.component.html',
})
export class JobsTableComponent {
  @Input({ required: true }) jobs!: Job[];
  @Input({ required: false }) displayDetails = true;

  private readonly modalService = inject(ModalService);
  private readonly router = inject(Router);

  readonly statusLabels: Record<JobStatus, String> = {
    COMPLETED: 'COMPLETED',
    ERROR: 'ERROR',
    ON_GOING: 'ON GOING'
  }

  readonly typeLabels: Record<JobType, String> = {
    DOWNLOAD: 'DOWNLOAD',
    DOWNLOAD_LIST: 'DOWNLOAD LIST',
    SCAN_LIB: 'SCAN LIB',
    DELETE: 'DELETE',
    DELETE_LIB: 'DELETE LIB',
    CLEAN_LIB: 'CLEAN LIB'
  }

  trackJobs(index: number, job: Job): string {
    return job.timeStamp.toString();
  }

  gotToJobPage(job: Job) {
    this.router.navigate(['settings', 'job', job.jobId, 'details']);
  }
}

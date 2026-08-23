import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { JobFilter, JobSortAttribute } from 'interfaces';
import { BehaviorSubject } from 'rxjs';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { TopBarService, UsersService } from 'services';
import { CommonModule } from '@angular/common';
import { RightPanelFunnelButtonComponent } from '../right-panel-funnel-button/right-panel-funnel-button.component';
import { RightPanelTitleComponent } from '../right-panel-title/right-panel-title.component';
import { BooleanSliderFormComponent } from '../boolean-slider-form/boolean-slider-form.component';
import { DateTextFormComponent } from '../date-text-form/date-text-form.component';

@UntilDestroy()
@Component({
  selector: 'app-job-funnel-button',
  imports: [
    CommonModule,
    RightPanelFunnelButtonComponent,
    RightPanelTitleComponent,
    ReactiveFormsModule,
    BooleanSliderFormComponent,
    DateTextFormComponent,
  ],
  templateUrl: './job-funnel-button.component.html',
})
export class JobFunnelButtonComponent implements OnInit {
  isDrawerOpen$ = new BehaviorSubject<boolean>(false);
  form: FormGroup;

  readonly users: string[] = [];

  @Output() filterChange = new EventEmitter<JobFilter>();
  @Output() contentChange = new EventEmitter<boolean>();

  private readonly formBuilder = inject(FormBuilder);
  private readonly topBarService = inject(TopBarService);
  private readonly usersService = inject(UsersService);

  readonly jobSortAttributes: JobSortAttribute[] = [
    'LATEST',
    'STATUS',
    'TYPE',
    'DURATION',
    'USERNAME',
  ];

  readonly initForm: JobFilterFunnel = {
    jobSortAttribute: 'LATEST',
    sortDescendingDirection: false,
    username: '',
    types: {
      download: true,
      downloadList: true,
      delete: true,
      scanLib: true,
      cleanLib: true,
      deleteLib: true,
    },
    status: {
      onGoing: true,
      completed: true,
      error: true,
      interrupted: true,
    },
    dateStart: '',
    dateEnd: '',
  };

  constructor() {
    this.form = this.formBuilder.group({
      jobSortAttribute: new FormControl('LATEST'),
      sortDescendingDirection: new FormControl(false),
      username: new FormControl(''),
      types: this.formBuilder.group({
        download: new FormControl(true),
        downloadList: new FormControl(true),
        delete: new FormControl(true),
        scanLib: new FormControl(true),
        cleanLib: new FormControl(true),
        deleteLib: new FormControl(true),
      }),
      status: this.formBuilder.group({
        onGoing: new FormControl(true),
        completed: new FormControl(true),
        error: new FormControl(true),
        interrupted: new FormControl(true),
      }),
      dateStart: new FormControl(''),
      dateEnd: new FormControl(''),
    });

    this.form.valueChanges.pipe(untilDestroyed(this)).subscribe((value: JobFilterFunnel) => {
      this.filterChange.emit(this.mapJobFilterToFunnel(value));
    });
  }
  ngOnInit(): void {
    this.form.patchValue(this.initForm, { emitEvent: true });
    this.topBarService.resetJobFilterFunnelFormEvent$
      .pipe(untilDestroyed(this))
      .subscribe((filter) => {
        this.form.patchValue(this.initForm, { emitEvent: true });
      });

    this.usersService
      .allUsers()
      .pipe(untilDestroyed(this))
      .subscribe((users) => {
        this.users.push(...users.map((user) => user.username));
      });
  }

  onOpenDrawer() {
    this.isDrawerOpen$.next(true);
    this.topBarService.setDrawerOpenState(true);
  }

  onCloseDrawer() {
    this.isDrawerOpen$.next(false);
    this.topBarService.setDrawerOpenState(false);
  }

  private mapJobFilterToFunnel(jobFilterFunnel: JobFilterFunnel): JobFilter {
    return {
      jobSortAttribute: jobFilterFunnel.jobSortAttribute,
      sortDescendingDirection: jobFilterFunnel.sortDescendingDirection,
      username: jobFilterFunnel.username || '',
      types: { ...jobFilterFunnel.types },
      status: { ...jobFilterFunnel.status },
      dateStart: jobFilterFunnel.dateStart ? new Date(jobFilterFunnel.dateStart) : undefined,
      dateEnd: jobFilterFunnel.dateEnd ? new Date(jobFilterFunnel.dateEnd) : undefined,
    };
  }
}

type JobFilterFunnel = {
  jobSortAttribute: JobSortAttribute;
  sortDescendingDirection: boolean;
  username: string;
  types: {
    download: boolean;
    downloadList: boolean;
    delete: boolean;
    scanLib: boolean;
    cleanLib: boolean;
    deleteLib: boolean;
  };
  status: {
    onGoing: boolean;
    completed: boolean;
    error: boolean;
    interrupted: boolean;
  };
  dateStart: string;
  dateEnd: string;
};

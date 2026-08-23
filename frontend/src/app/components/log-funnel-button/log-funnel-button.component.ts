import { Component, EventEmitter, inject, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BehaviorSubject } from 'rxjs';
import { TopBarService, UsersService } from 'services';
import { LogFilter, LogSortAttribute, Severities } from 'src/app/interfaces/log';
import { DateTextFormComponent } from '../date-text-form/date-text-form.component';
import { BooleanSliderFormComponent } from '../boolean-slider-form/boolean-slider-form.component';
import { RightPanelTitleComponent } from '../right-panel-title/right-panel-title.component';
import { RightPanelFunnelButtonComponent } from '../right-panel-funnel-button/right-panel-funnel-button.component';
import { CommonModule } from '@angular/common';
import { InputTextFormComponent } from '../input-text-form/input-text-form.component';

@UntilDestroy()
@Component({
  selector: 'app-log-funnel-button',
  imports: [
    CommonModule,
    RightPanelFunnelButtonComponent,
    RightPanelTitleComponent,
    ReactiveFormsModule,
    BooleanSliderFormComponent,
    DateTextFormComponent,
    InputTextFormComponent,
  ],
  templateUrl: './log-funnel-button.component.html',
})
export class LogFunnelButtonComponent {
  isDrawerOpen$ = new BehaviorSubject<boolean>(false);
  form: FormGroup;

  readonly users: string[] = [];

  @Output() filterChange = new EventEmitter<LogFilter>();
  @Output() contentChange = new EventEmitter<boolean>();

  private readonly formBuilder = inject(FormBuilder);
  private readonly topBarService = inject(TopBarService);
  private readonly usersService = inject(UsersService);

  readonly logSortAttributes: LogSortAttribute[] = ['LATEST', 'SEVERITY', 'USERNAME'];

  readonly initForm: LogFilterFunnel = {
    logSortAttribute: 'LATEST',
    sortDescendingDirection: false,
    username: '',
    messageContains: '',
    detailsContains: '',
    severities: {
      info: true,
      error: true,
      warning: true,
    },
    dateStart: '',
    dateEnd: '',
  };

  constructor() {
    this.form = this.formBuilder.group({
      logSortAttribute: new FormControl('LATEST'),
      sortDescendingDirection: new FormControl(false),
      username: new FormControl(''),
      messageContains: new FormControl(''),
      detailsContains: new FormControl(''),
      severities: this.formBuilder.group({
        info: new FormControl(true),
        error: new FormControl(true),
        warning: new FormControl(true),
      }),
      dateStart: new FormControl(''),
      dateEnd: new FormControl(''),
    });

    this.form.valueChanges.pipe(untilDestroyed(this)).subscribe((value: LogFilterFunnel) => {
      this.filterChange.emit(this.mapLogFilterToFunnel(value));
    });
  }
  ngOnInit(): void {
    this.form.patchValue(this.initForm, { emitEvent: true });
    this.topBarService.resetLogFilterFunnelFormEvent$
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

  private mapLogFilterToFunnel(logFilterFunnel: LogFilterFunnel): LogFilter {
    return {
      logSortAttribute: logFilterFunnel.logSortAttribute,
      sortDescendingDirection: logFilterFunnel.sortDescendingDirection,
      username: logFilterFunnel.username || '',
      messageContains: logFilterFunnel.messageContains || null,
      detailsContains: logFilterFunnel.detailsContains || null,
      severities: { ...logFilterFunnel.severities },
      dateStart: logFilterFunnel.dateStart ? new Date(logFilterFunnel.dateStart) : undefined,
      dateEnd: logFilterFunnel.dateEnd ? new Date(logFilterFunnel.dateEnd) : undefined,
    };
  }
}

type LogFilterFunnel = {
  logSortAttribute: LogSortAttribute;
  sortDescendingDirection: boolean;
  username: string | null;
  messageContains: string | null;
  detailsContains: string | null;
  severities: Severities;
  dateStart: string;
  dateEnd: string;
};

import { Component, EventEmitter, inject, Output } from '@angular/core';
import { RightPanelFunnelButtonComponent } from '../right-panel-funnel-button/right-panel-funnel-button.component';
import { BehaviorSubject } from 'rxjs';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';
import { LocalStorageService } from 'src/app/services/local-storage.service';
import { TopBarService } from 'src/app/services/top-bar.service';
import { NavigationEnd, Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { RightPanelTitleComponent } from '../right-panel-title/right-panel-title.component';
import { TwoChoicesChipComponent } from '../two-choices-chip/two-choices-chip.component';
import { BooleanSliderFormComponent } from '../boolean-slider-form/boolean-slider-form.component';
import { DigitInputFormComponent } from '../digit-input-form/digit-input-form.component';
import { CommonModule } from '@angular/common';
import { UsersService } from 'src/app/services/users.service';
import { ComicSortAttribute, SeriesSortAttribute } from 'interfaces';
import { DateTextFormComponent } from '../date-text-form/date-text-form.component';

@UntilDestroy()
@Component({
  selector: 'app-advance-funnel-button',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    BooleanSliderFormComponent,
    DigitInputFormComponent,
    TwoChoicesChipComponent,
    RightPanelTitleComponent,
    RightPanelFunnelButtonComponent,
    DateTextFormComponent,
  ],
  templateUrl: './advance-funnel-button.component.html',
})
export class AdvanceFunnelButtonComponent {
  isDrawerOpen$ = new BehaviorSubject<boolean>(false);
  form: FormGroup;

  @Output() filterChange = new EventEmitter<AdvanceFilterFunnel>();
  @Output() contentChange = new EventEmitter<boolean>();

  private readonly formBuilder = inject(FormBuilder);
  private readonly localStorageService = inject(LocalStorageService);
  private readonly topBarService = inject(TopBarService);
  private readonly router = inject(Router);
  private readonly usersService = inject(UsersService);

  seriesContent: boolean = true;
  readonly users: string[] = [];

  readonly comicSortAttributes: ComicSortAttribute[] = [
    'LATEST',
    'TITLE',
    'YEAR',
    'CATEGORY',
    'SIZE',
  ];
  readonly seriesSortAttributes: SeriesSortAttribute[] = [
    'LATEST',
    'TITLE',
    'YEAR',
    'CATEGORY',
    'ISSUES',
  ];

  readonly initForm: AdvanceFilterFunnel = {
    comicSortAttribute: 'LATEST',
    seriesSortAttribute: 'LATEST',
    sortDescendingDirection: false,
    categories: {
      marvel: true,
      dc: true,
      other: true,
    },
    usersReadStatus: [],
    year: {
      from: '',
      to: '',
    },
    size: {
      from: '',
      to: '',
    },
    issues: {
      from: '',
      to: '',
    },
    createdAt: {
      from: '',
      to: '',
    },
    modifiedAt: {
      from: '',
      to: '',
    },
  };

  constructor() {
    this.form = this.formBuilder.group({
      comicSortAttribute: new FormControl('LATEST'),
      seriesSortAttribute: new FormControl('LATEST'),
      sortDescendingDirection: new FormControl(false),
      categories: this.formBuilder.group({
        marvel: new FormControl(true),
        dc: new FormControl(true),
        other: new FormControl(true),
      }),
      usersReadStatus: this.formBuilder.array([]),
      year: this.formBuilder.group({
        from: new FormControl(''),
        to: new FormControl(''),
      }),
      size: this.formBuilder.group({
        from: new FormControl(''),
        to: new FormControl(''),
      }),
      issues: this.formBuilder.group({
        from: new FormControl(''),
        to: new FormControl(''),
      }),
      createdAt: this.formBuilder.group({
        from: new FormControl(''),
        to: new FormControl(''),
      }),
      modifiedAt: this.formBuilder.group({
        from: new FormControl(''),
        to: new FormControl(''),
      }),
    });

    this.form.valueChanges.subscribe((value) => {
      this.filterChange.emit(value);
    });
  }

  ngOnInit(): void {
    this.form.patchValue(this.initForm, { emitEvent: true });
    this.topBarService.resetAdvancedFilterFunnelFormEvent$
      .pipe(untilDestroyed(this))
      .subscribe((filter) => {
        this.form.patchValue(this.initForm, { emitEvent: true });
      });

    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const routeString = event.urlAfterRedirects;
        this.seriesContent = routeString.startsWith('/advanceLibrary/series');
        this.contentChange.emit(this.seriesContent);
      }
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
  }

  onCloseDrawer() {
    this.isDrawerOpen$.next(false);
  }

  OnContentTypeSeries(value: boolean) {
    this.topBarService.setSearchBarTextLib('');
    this.contentChange.emit(value);
    this.seriesContent = value;
    this.localStorageService.setItem('advanceContentSeries', value);
    const route = ['advanceLibrary', value ? 'series' : 'issues'];
    this.router.navigate(route);
  }

  get usersReadStatusArray(): FormArray {
    return this.form.get('usersReadStatus') as FormArray;
  }

  addUserRule(): void {
    this.usersReadStatusArray.push(
      this.formBuilder.group({
        userName: new FormControl(this.users[0] || ''),
        notStarted: new FormControl(true),
        ongoing: new FormControl(true),
        read: new FormControl(true),
      }),
    );
  }

  removeUserRule(index: number): void {
    this.usersReadStatusArray.removeAt(index);
  }
}

export type AdvanceFilterFunnel = {
  comicSortAttribute: ComicSortAttribute;
  seriesSortAttribute: SeriesSortAttribute;
  sortDescendingDirection: boolean;
  categories: {
    marvel: boolean;
    dc: boolean;
    other: boolean;
  };
  usersReadStatus: {
    userName: string;
    notStarted: boolean;
    ongoing: boolean;
    read: boolean;
  }[];
  year: {
    from: number | '';
    to: number | '';
  };
  size: {
    from: number | '';
    to: number | '';
  };
  issues: {
    from: number | '';
    to: number | '';
  };
  createdAt: {
    from: Date | '';
    to: Date | '';
  };
  modifiedAt: {
    from: Date | '';
    to: Date | '';
  };
};

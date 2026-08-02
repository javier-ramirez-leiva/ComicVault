import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, Output, inject } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { LocalStorageService } from 'services';
import { TopBarService } from 'services';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BooleanSliderFormComponent } from '../boolean-slider-form/boolean-slider-form.component';
import { DigitInputFormComponent } from '../digit-input-form/digit-input-form.component';
import { TwoChoicesChipComponent } from '../two-choices-chip/two-choices-chip.component';
import { RightPanelTitleComponent } from '../right-panel-title/right-panel-title.component';
import { RightPanelFunnelButtonComponent } from '../right-panel-funnel-button/right-panel-funnel-button.component';
import { NavigationEnd, Router } from '@angular/router';
import { ComicSortAttribute, SeriesSortAttribute } from 'src/app/interfaces/comics';
import { DateTextFormComponent } from '../date-text-form/date-text-form.component';

@UntilDestroy()
@Component({
  selector: 'app-funnel-button',
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
  templateUrl: './funnel-button.component.html',
})
export class FunnelButtonComponent {
  isDrawerOpen$ = new BehaviorSubject<boolean>(false);
  form: FormGroup;

  @Output() filterChange = new EventEmitter<FilterFunnel>();
  @Output() contentChange = new EventEmitter<boolean>();

  private readonly formBuilder = inject(FormBuilder);
  private readonly localStorageService = inject(LocalStorageService);
  private readonly topBarService = inject(TopBarService);
  private readonly router = inject(Router);

  seriesContent: boolean = true;

  readonly comicSortAttributes: ComicSortAttributeFront[] = [
    'LATEST',
    'TITLE',
    'YEAR',
    'CATEGORY',
    'SIZE',
    'READ STATUS',
  ];
  readonly seriesSortAttributes: SeriesSortAttributeFront[] = [
    'LATEST',
    'TITLE',
    'YEAR',
    'CATEGORY',
    'ISSUES',
  ];

  readonly initForm: FilterFunnel = {
    comicSortAttribute: 'LATEST',
    seriesSortAttribute: 'LATEST',
    sortDescendingDirection: false,
    categories: {
      marvel: true,
      dc: true,
      other: true,
    },
    readStatus: {
      notStarted: true,
      ongoing: true,
      read: true,
    },
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
      readStatus: this.formBuilder.group({
        notStarted: new FormControl(true),
        ongoing: new FormControl(true),
        read: new FormControl(true),
      }),
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
    this.topBarService.resetFilterFunnelFormEvent$
      .pipe(untilDestroyed(this))
      .subscribe((filter) => {
        this.form.patchValue(this.initForm, { emitEvent: true });
      });

    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const routeString = event.urlAfterRedirects;
        this.seriesContent = routeString.startsWith('/library/series');
        this.contentChange.emit(this.seriesContent);
      }
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

  OnContentTypeSeries(value: boolean) {
    this.topBarService.setSearchBarTextLib('');
    this.contentChange.emit(value);
    this.seriesContent = value;
    this.localStorageService.setItem('contentSeries', value);
    const route = ['library', value ? 'series' : 'issues'];
    this.router.navigate(route);
  }
}

export type ComicSortAttributeFront = ComicSortAttribute | 'READ STATUS';
export type SeriesSortAttributeFront = SeriesSortAttribute;

export type FilterFunnel = {
  comicSortAttribute: ComicSortAttribute;
  seriesSortAttribute: SeriesSortAttribute;
  sortDescendingDirection: boolean;
  categories: {
    marvel: boolean;
    dc: boolean;
    other: boolean;
  };
  readStatus: {
    notStarted: boolean;
    ongoing: boolean;
    read: boolean;
  };
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

import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, OnInit, Output, inject } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { TopBarService } from 'services';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { FilterHistory } from 'interfaces';
import { BooleanSliderFormComponent } from '../boolean-slider-form/boolean-slider-form.component';
import { RightPanelFunnelButtonComponent } from "../right-panel-funnel-button/right-panel-funnel-button.component";
import { RightPanelTitleComponent } from "../right-panel-title/right-panel-title.component";
import { InputTextFormComponent } from "../input-text-form/input-text-form.component";
import { DateTextFormComponent } from "../date-text-form/date-text-form.component";
import { ActivatedRoute, Router } from '@angular/router';

@UntilDestroy()
@Component({
  selector: 'app-funnel-button-history',
  imports: [CommonModule, ReactiveFormsModule, BooleanSliderFormComponent, RightPanelFunnelButtonComponent, RightPanelTitleComponent, InputTextFormComponent, DateTextFormComponent],
  templateUrl: './funnel-button-history.component.html',
})
export class FunnelButtonHistoryComponent implements AfterViewInit {

  isDrawerOpen$ = new BehaviorSubject<boolean>(false);
  form: FormGroup;

  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  @Output() filterChange = new EventEmitter<FilterHistory>();

  constructor() {
    this.form = this.formBuilder.group({
      comicTitle: new FormControl(''),
      dateStart: new FormControl(''),
      dateEnd: new FormControl(''),
      readStatusOnGoing: new FormControl(true),
      readStatusRead: new FormControl(true),
      inLibraryYes: new FormControl(true),
      inLibraryNo: new FormControl(true),
    });
  }

  ngAfterViewInit(): void {
    this.route.queryParams.pipe(
      untilDestroyed(this)
    ).subscribe(params => {
      const filterHistory = {
        comicTitle: params['comicTitle'] || '',
        dateStart: params['dateStart'] || '',
        dateEnd: params['dateEnd'] || '',
        readStatusOnGoing: params['readStatusOnGoing'] ? params['readStatusOnGoing'] === 'true' : true,
        readStatusRead: params['readStatusRead'] ? params['readStatusRead'] === 'true' : true,
        inLibraryYes: params['inLibraryYes'] ? params['inLibraryYes'] === 'true' : true,
        inLibraryNo: params['inLibraryNo'] ? params['inLibraryNo'] === 'true' : true,
      };
      this.form.setValue(filterHistory);
    });
  }

  onOpenDrawer() {
    this.isDrawerOpen$.next(true);
  }

  onCloseDrawer() {
    this.isDrawerOpen$.next(false);
    this.filterChange.emit(this.form.value);
    this.router.navigate([], {
      queryParams: this.form.value,
    });
  }
}



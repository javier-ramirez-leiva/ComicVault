import { Component, inject, Input, OnInit } from '@angular/core';
import { ComicsDatabase, ModalComponent, Role } from 'interfaces';
import { CrossModalComponent } from '../cross-modal/cross-modal.component';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

import {
  ComicsService,
  NotifierService,
  READER_SETTINGS_BOOLEANS,
  ReaderSettingsBooleanMap,
  ReaderSettingsBooleans,
  ReaderSettingsService,
} from 'services';
import { BooleanSliderFormComponent } from '../boolean-slider-form/boolean-slider-form.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { HideRolesDirective } from 'directives';

export type ReaderSettings = {
  readerSettingsService: ReaderSettingsBooleanMap;
};

@Component({
  selector: 'app-modal-reader-settings',
  imports: [
    CrossModalComponent,
    ReactiveFormsModule,
    BooleanSliderFormComponent,
    HideRolesDirective
],
  templateUrl: './modal-reader-settings.component.html',
})
@UntilDestroy()
export class ModalReaderSettings
  implements ModalComponent<ReaderSettings, { comic: ComicsDatabase; page: number }>, OnInit
{
  @Input({ required: true }) data?: { comic: ComicsDatabase; page: number };

  close!: (response?: ReaderSettings) => void;

  form: FormGroup;

  readonly Role = Role;

  private readonly formBuilder = inject(FormBuilder);
  private readonly readerSettingsService = inject(ReaderSettingsService);
  private readonly comicsService = inject(ComicsService);
  private readonly notifierService = inject(NotifierService);

  constructor() {
    this.form = this.formBuilder.group({
      readerSettingsService: this.formBuilder.group(
        Object.fromEntries(READER_SETTINGS_BOOLEANS.map((k) => [k, new FormControl(true)])) as {
          [K in ReaderSettingsBooleans]: FormControl<boolean>;
        },
      ),
      doublePageConfiguration: new FormControl(''),
    });

    this.form.valueChanges.pipe(untilDestroyed(this)).subscribe((value) => {
      this.readerSettingsService.set(value.readerSettingsService);
      this.readerSettingsService.setDoublePageConfiguration(value.doublePageConfiguration);
    });
  }

  ngOnInit(): void {
    const modal = {
      readerSettingsService: this.readerSettingsService.get(),
      doublePageConfiguration: this.readerSettingsService.getDoublePageConfiguration(),
    };
    this.form.patchValue(modal);
  }

  setCover() {
    if (this.data) {
      this.comicsService
        .setCover(this.data.comic.id, this.data.page)
        .pipe(untilDestroyed(this))
        .subscribe(() => {
          this.notifierService.appendNotification({
            id: 0,
            title: 'Cover update:',
            message: `${this.data?.comic.title}`,
            type: 'success',
          });
        });
    }
  }

  cancel() {
    this.close(undefined);
  }
}

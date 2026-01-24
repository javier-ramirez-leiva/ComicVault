import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { ModalService, ReaderSettingsService } from 'services';
import {
  ModalReaderSettings,
  ReaderSettings,
} from '../modal-reader-settings/modal-reader-settings.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { filter } from 'rxjs';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { ComicsDatabase } from 'interfaces';

@Component({
  selector: 'app-reader-settings-button',
  imports: [],
  templateUrl: './reader-settings-button.component.html',
})
@UntilDestroy()
export class ReaderSettingsButtonComponent {
  @Input({ required: true }) comic!: ComicsDatabase;
  @Input({ required: true }) page!: number;
  @Output() onClosed = new EventEmitter<void>();

  private readonly modalService = inject(ModalService);
  private readonly readerSettingsService = inject(ReaderSettingsService);

  displaySettings() {
    this.modalService
      .open<ReaderSettings, { comic: ComicsDatabase; page: number }>(ModalReaderSettings, {
        comic: this.comic,
        page: this.page,
      })
      .pipe(untilDestroyed(this))
      .subscribe(() => this.onClosed.emit());
  }
}

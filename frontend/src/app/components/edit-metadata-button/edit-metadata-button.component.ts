import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { isHttpResponseError } from 'interfaces';
import { catchError, EMPTY, of } from 'rxjs';
import { ComicVineIssueInfo } from 'src/app/interfaces/comicVine';
import { ComicVineService } from 'src/app/services/comic-vine.service';
import { NotifierService } from 'src/app/services/notifier.service';

@UntilDestroy()
@Component({
  selector: 'app-edit-metadata-button',
  imports: [],
  templateUrl: './edit-metadata-button.component.html',
})
export class EditMetadataButtonComponent {
  @Input() issueTitle: string | undefined = undefined;

  @Output() onEdit = new EventEmitter<void>();
  @Output() onSave = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();
  @Output() onComicVine = new EventEmitter<ComicVineIssueInfo | null>();

  private readonly comicVineService = inject(ComicVineService);
  private readonly notifierService = inject(NotifierService);

  protected editStatus = false;

  click() {
    this.editStatus = !this.editStatus;
    this.onEdit.emit();
  }

  ok() {
    this.editStatus = !this.editStatus;
    this.onSave.emit();
  }

  comicVine() {
    this.comicVineService
      .searchIssue(this.issueTitle || '')
      .pipe(
        catchError((catchError) => {
          const error = catchError.error;
          if (isHttpResponseError(error)) {
            this.notifierService.appendNotification({
              id: 0,
              title: 'Error fetching data from ComicVine',
              message: catchError.error.message as string,
              type: 'error',
            });
          }
          return EMPTY;
        }),
        untilDestroyed(this),
      )
      .subscribe((issueInfo) => {
        this.onComicVine.emit(issueInfo);
      });
  }

  cancel() {
    this.editStatus = !this.editStatus;
    this.onCancel.emit();
  }
}

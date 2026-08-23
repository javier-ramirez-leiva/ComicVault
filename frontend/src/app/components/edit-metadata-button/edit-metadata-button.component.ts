import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ComicVineIssueInfo } from 'src/app/interfaces/comicVine';
import { ComicVineService } from 'src/app/services/comic-vine.service';

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
      .pipe(untilDestroyed(this))
      .subscribe((issueInfo) => {
        this.onComicVine.emit(issueInfo);
      });
  }

  cancel() {
    this.editStatus = !this.editStatus;
    this.onCancel.emit();
  }
}

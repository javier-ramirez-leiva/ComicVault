import { Component, inject, Input } from '@angular/core';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ModalHelpComponent } from '../modal-help/modal-help.component';
import { ModalService } from 'services';

@Component({
  selector: 'app-help-button',
  imports: [],
  templateUrl: './help-button.component.html',
})
@UntilDestroy()
export class HelpButtonComponent {
  @Input({ required: true }) message!: string;
  private readonly modalService = inject(ModalService);

  displayHelp() {
    this.modalService
      .open<boolean, { message: string | undefined }>(ModalHelpComponent, { message: this.message })
      .pipe(untilDestroyed(this))
      .subscribe();
  }
}

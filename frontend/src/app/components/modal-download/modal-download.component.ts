import { Component, Input } from '@angular/core';
import { ModalComponent } from 'interfaces';
import { DownloadLink } from 'interfaces';
import { CommonModule } from '@angular/common';
import { RadioButtonInputComponent } from "../radio-button-input/radio-button-input.component";
import { CrossModalComponent } from "../cross-modal/cross-modal.component";

@Component({
  selector: 'app-modal-download',
  imports: [CommonModule, RadioButtonInputComponent, CrossModalComponent],
  templateUrl: './modal-download.component.html'
})
export class ModalDownloadComponent implements ModalComponent<DownloadLink, { options: DownloadLink[] | undefined; comicTitle: string }> {
  @Input({ required: true }) data?: { options: DownloadLink[] | undefined; comicTitle: string };

  private link: DownloadLink | undefined;

  close!: (response?: DownloadLink) => void;

  confirm() {
    this.close(this.link);
  }

  cancel() {
    this.close(undefined);
  }

  updateLink(link: DownloadLink) {
    this.link = link;
  }

}

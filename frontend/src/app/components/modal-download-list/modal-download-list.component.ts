import { Component, Input, OnInit } from '@angular/core';
import { DownloadIssue, DownloadIssueRequest, DownloadLink } from 'interfaces';
import { ModalComponent } from 'interfaces';
import { CommonModule } from '@angular/common';
import { CrossModalComponent } from "../cross-modal/cross-modal.component";

@Component({
  selector: 'app-modal-download-list',
  imports: [CommonModule, CrossModalComponent],
  templateUrl: './modal-download-list.component.html'
})
export class ModalDownloadListComponent implements ModalComponent<DownloadIssueRequest[], { options: DownloadIssue[] | undefined; comicTitle: string }>, OnInit {
  @Input({ required: true }) data?: { options: DownloadIssue[] | undefined; comicTitle: string };

  private requests: DownloadIssueRequest[] = [];

  ngOnInit(): void {
    this.requests = this.data?.options?.map(issue => {
      return {
        description: issue.description,
        idGcIssue: issue.idGcIssue,
        title: issue.title,
        link: issue.links ? issue.links[0] : undefined,
      }
    }) ?? [];
  }

  close!: (response?: DownloadIssueRequest[]) => void;

  confirm() {
    this.close(this.requests);
  }

  cancel() {
    this.close(undefined);
  }

  onPlatformChange(event: Event, idGC: string) {
    const value = +(event.target as HTMLSelectElement).value;

    const selected = this.requests.find(issue => issue.idGcIssue === idGC);

    if (!selected) {
      console.error('Selected link not found');
      return;
    }
    if (value === -1) {
      selected.link = undefined;
    } else {
      selected.link = this.data?.options?.find(issue => issue.idGcIssue === idGC)?.links[value];
    }
  }
}

import { Component, inject, Input, OnInit } from '@angular/core';
import { ComicsDatabase, ModalComponent } from 'interfaces';
import { CrossModalComponent } from '../cross-modal/cross-modal.component';

import { range } from 'src/app/utils/number';
import { ConfigURLService } from 'services';

@Component({
  selector: 'app-modal-mini-pages',
  imports: [CrossModalComponent],
  templateUrl: './modal-mini-pages.component.html',
})
export class ModalMiniPagesComponent
  implements ModalComponent<number, { comic: ComicsDatabase }>, OnInit
{
  @Input({ required: true }) data?: { comic: ComicsDatabase };

  private readonly configURLService = inject(ConfigURLService);

  page: number = 0;
  protected imageErrors: boolean[] = [];
  protected pagesURL: string = '';

  close!: (response?: number) => void;

  range: number[] = [];

  ngOnInit(): void {
    if (this.data) {
      this.range = range(this.data.comic.pages);
      this.pagesURL = `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${this.data.comic.id}/minipages/`;
    }
  }

  confirm() {
    this.close(this.page);
  }

  cancel() {
    this.close(undefined);
  }

  updatePage(page: number) {
    this.page = page;
  }
}

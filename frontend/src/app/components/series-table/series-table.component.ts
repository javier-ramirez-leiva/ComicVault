import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Series } from 'interfaces';
import { ConfigURLService } from 'services';
import { PublisherCellComponent } from "../publisher-cell/publisher-cell.component";

@Component({
  selector: 'app-series-table',
  imports: [CommonModule, RouterLink, PublisherCellComponent],
  templateUrl: './series-table.component.html',
})
export class SeriesTableComponent {
  @Input({ required: true }) series!: Series[];

  protected readonly configURLService = inject(ConfigURLService);

  getSeriesImgUrl(series: Series): string {
    const comicID = series.comics[0].id;
    return `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${comicID}/cover/small`;
  }
}

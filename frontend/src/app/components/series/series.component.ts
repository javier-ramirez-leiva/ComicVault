import { Component, inject, Input, OnInit } from '@angular/core';
import { Series } from 'interfaces';
import { CoverCardComponent } from '../cover-card/cover-card.component';
import { ConfigURLService } from 'services';

@Component({
  selector: 'app-series',
  imports: [CoverCardComponent],
  templateUrl: './series.component.html',
})
export class SeriesComponent implements OnInit {
  @Input() public series!: Series;

  imageURL!: string;
  configURLService = inject(ConfigURLService);
  url: string[] = [];
  number: number | undefined = undefined;

  ngOnInit(): void {
    this.imageURL = `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/series/${this.series.id}/cover/small`;
    this.url = ['/series', this.series.id, 'details'];
    this.number = !this.series.readStatus
      ? this.series.totalIssues - this.series.readIssues
      : undefined;
  }
}

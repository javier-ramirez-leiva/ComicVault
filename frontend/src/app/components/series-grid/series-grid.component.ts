import { Component, Input } from '@angular/core';
import { SeriesComponent } from "../series/series.component";
import { Series } from 'interfaces';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-series-grid',
  imports: [SeriesComponent, CommonModule],
  templateUrl: './series-grid.component.html'
})
export class SeriesGridComponent {
  @Input() public series!: Series[];

  trackBySeries(index: number, series: Series): string {
    return series.id;
  }
}

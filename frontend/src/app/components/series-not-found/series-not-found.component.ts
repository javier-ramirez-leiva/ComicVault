
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-series-not-found',
  imports: [],
  templateUrl: './series-not-found.component.html',
})
export class SeriesNotFoundComponent {
  @Input({ required: false }) id: string | undefined;
}

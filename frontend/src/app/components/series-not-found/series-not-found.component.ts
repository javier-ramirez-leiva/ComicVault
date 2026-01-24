import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-series-not-found',
  imports: [CommonModule],
  templateUrl: './series-not-found.component.html',
})
export class SeriesNotFoundComponent {
  @Input({ required: false }) id: string | undefined;
}

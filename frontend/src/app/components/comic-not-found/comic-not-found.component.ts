import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-comic-not-found',
  imports: [],
  templateUrl: './comic-not-found.component.html',
})
export class ComicNotFoundComponent {
  @Input({ required: false }) id: string | undefined;
}

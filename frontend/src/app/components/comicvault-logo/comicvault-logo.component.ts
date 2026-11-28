import { Component } from '@angular/core';

let nextLogoId = 0;

@Component({
  selector: 'app-comicvault-logo',
  standalone: true,
  templateUrl: './comicvault-logo.component.html',
  host: { class: 'cv-logo', 'aria-hidden': 'true' },
})
export class ComicvaultLogoComponent {
  protected readonly maskId = `cv-logo-mask-${nextLogoId++}`;
}

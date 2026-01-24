import { Component, inject } from '@angular/core';
import { DarkmodeService } from 'services';

@Component({
  selector: 'app-dark-mode-button',
  imports: [],
  templateUrl: './dark-mode-button.component.html',
})
export class DarkModeButtonComponent {
  darkModeService = inject(DarkmodeService);
}

import { Component, inject } from '@angular/core';
import { DarkmodeService } from 'services';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dark-mode-button',
  imports: [CommonModule],
  templateUrl: './dark-mode-button.component.html'
})
export class DarkModeButtonComponent {
  darkModeService = inject(DarkmodeService);
}

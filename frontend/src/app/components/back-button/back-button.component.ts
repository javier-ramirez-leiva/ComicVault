import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'app-back-button',
  imports: [],
  templateUrl: './back-button.component.html',
})
export class BackButtonComponent {
  private readonly router = inject(Router);
  private readonly location = inject(Location);

  goBack() {
    this.location.back();
  }
}

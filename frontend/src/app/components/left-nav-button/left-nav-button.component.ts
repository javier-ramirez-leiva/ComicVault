import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { take } from 'rxjs';
import { ActivePageService } from 'services';

@UntilDestroy()
@Component({
  selector: 'app-left-nav-button',
  imports: [CommonModule, RouterModule],
  templateUrl: './left-nav-button.component.html'
})
export class LeftNavButtonComponent {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) route!: string;
  @Input({ required: true }) activeRoute!: string;
  @Input({ required: true }) svgPath!: string;
  @Input({ required: false }) queryParams: any = undefined;

  protected readonly activePageService = inject(ActivePageService);
}

import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { ActivePageService } from 'services';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-bottom-drawer-button',
  imports: [CommonModule, RouterModule],
  templateUrl: './bottom-drawer-button.component.html',
})
export class BottomDrawerButtonComponent {
  @Input({ required: true }) route!: string;
  @Input({ required: true }) activeRoute!: string;
  @Input({ required: true }) svgPath!: string;
  @Input({ required: false }) queryParams: any = undefined;

  protected readonly activePageService = inject(ActivePageService);
}

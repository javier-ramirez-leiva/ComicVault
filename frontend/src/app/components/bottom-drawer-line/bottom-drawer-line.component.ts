import { Component, Input, inject } from '@angular/core';
import { ActivePageService } from 'services';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-bottom-drawer-line',
  imports: [CommonModule, RouterModule],
  templateUrl: './bottom-drawer-line.component.html',
})
export class BottomDrawerLineComponent {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) route!: string;
  @Input({ required: true }) activeRoute!: string;
  @Input({ required: false }) queryParams: any = undefined;
  protected readonly activePageService = inject(ActivePageService);
}

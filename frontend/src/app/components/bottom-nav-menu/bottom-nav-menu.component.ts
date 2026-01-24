import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivePageService, BlurMaskService } from 'services';
import { HideRolesDirective, OutsideClickDirective } from 'directives';
import { Role } from 'interfaces';
import { BottomDrawerLineComponent } from '../bottom-drawer-line/bottom-drawer-line.component';
import { BottomDrawerButtonComponent } from '../bottom-drawer-button/bottom-drawer-button.component';
import { BottomDrawerButtonsComponent } from '../bottom-drawer-buttons/bottom-drawer-buttons.component';
import { NavigationEnd, Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@Component({
  selector: 'app-bottom-nav-menu',
  imports: [
    CommonModule,
    HideRolesDirective,
    BottomDrawerLineComponent,
    BottomDrawerButtonsComponent,
    OutsideClickDirective,
  ],
  templateUrl: './bottom-nav-menu.component.html',
})
@UntilDestroy()
export class BottomNavMenuComponent {
  protected readonly activePageService = inject(ActivePageService);
  private readonly blurMaskService = inject(BlurMaskService);
  private readonly router = inject(Router);
  protected readonly Role = Role;
  protected drawerOpen = false;

  onDrawerOpen(value: boolean) {
    if (this.drawerOpen !== value) {
      this.drawerOpen = value;
      this.blurMaskService.setActive(value);
    }
  }

  constructor() {
    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.onDrawerOpen(false);
      }
    });
  }
}

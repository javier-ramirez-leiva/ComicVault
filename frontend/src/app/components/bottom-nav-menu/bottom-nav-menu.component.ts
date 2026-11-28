import { Component, HostListener, inject } from '@angular/core';
import { ActivePageService } from 'services';
import { HideRolesDirective } from 'directives';
import { Role } from 'interfaces';
import { BottomDrawerLineComponent } from '../bottom-drawer-line/bottom-drawer-line.component';
import { BottomDrawerButtonsComponent } from '../bottom-drawer-buttons/bottom-drawer-buttons.component';
import { NavigationEnd, Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@Component({
  selector: 'app-bottom-nav-menu',
  imports: [
    HideRolesDirective,
    BottomDrawerLineComponent,
    BottomDrawerButtonsComponent,
  ],
  templateUrl: './bottom-nav-menu.component.html',
})
@UntilDestroy()
export class BottomNavMenuComponent {
  protected readonly activePageService = inject(ActivePageService);
  private readonly router = inject(Router);
  protected readonly Role = Role;
  protected drawerOpen = false;

  onDrawerOpen(value: boolean) {
    if (this.drawerOpen !== value) {
      this.drawerOpen = value;
    }
  }

  @HostListener('window:resize')
  onResize() {
    if (window.innerWidth >= 768) this.onDrawerOpen(false);
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    this.onDrawerOpen(false);
  }

  constructor() {
    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.onDrawerOpen(false);
      }
    });
  }
}

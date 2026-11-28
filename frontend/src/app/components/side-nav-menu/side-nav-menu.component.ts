import { Component, HostListener, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { BlurMaskService, NotifierService } from 'services';
import { NotificationGridComponent } from '../notification-grid/notification-grid.component';
import { ProcessingNotificationGridComponent } from '../processing-notification-grid/processing-notification-grid.component';
import { CommonModule } from '@angular/common';
import { TopBarComponent } from '../top-bar/top-bar.component';
import { ModalContainerComponent } from '../modal-container/modal-container.component';
import { MultiSelectComponent } from '../multi-select/multi-select.component';
import { BottomNavMenuComponent } from '../bottom-nav-menu/bottom-nav-menu.component';
import { LeftNavMenuComponent } from '../left-nav-menu/left-nav-menu.component';
import { OutsideMenuClickDirective } from 'directives';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { BlurMaskComponent } from '../blur-mask/blur-mask.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@Component({
  selector: 'app-side-nav-menu',
  imports: [
    RouterOutlet,
    NotificationGridComponent,
    ProcessingNotificationGridComponent,
    CommonModule,
    TopBarComponent,
    ModalContainerComponent,
    MultiSelectComponent,
    BottomNavMenuComponent,
    LeftNavMenuComponent,
    OutsideMenuClickDirective,
    BlurMaskComponent,
  ],
  templateUrl: './side-nav-menu.component.html',
})
@UntilDestroy()
export class SideNavMenuComponent {
  protected readonly notifierService: NotifierService = inject(NotifierService);

  protected leftMenuOpenTrigger$ = new BehaviorSubject<boolean>(false);

  protected readonly leftMenuOpen$: Observable<'open' | 'close'>;

  protected readonly blurMaskService = inject(BlurMaskService);
  protected readonly router = inject(Router);
  isDesktop = window.matchMedia('(min-width: 1024px)').matches;

  constructor() {
    this.leftMenuOpen$ = this.leftMenuOpenTrigger$.pipe(
      map((open) => (open ? 'open' : 'close')),
    );

    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.setLeftMenuOpen(false);
      }
    });
  }

  @HostListener('window:resize')
  onResize() {
    this.isDesktop = window.matchMedia('(min-width: 1024px)').matches;
    if (this.isDesktop || window.innerWidth < 768) this.setLeftMenuOpen(false);
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    this.setLeftMenuOpen(false);
  }

  setLeftMenuOpen(value: boolean) {
    if (this.leftMenuOpenTrigger$.value !== value) this.leftMenuOpenTrigger$.next(value);
  }
}

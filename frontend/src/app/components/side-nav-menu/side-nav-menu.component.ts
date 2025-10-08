import { Component, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { BlurMaskService, NotifierService } from 'services';
import { NotificationGridComponent } from '../notification-grid/notification-grid.component';
import { ProcessingNotificationGridComponent } from '../processing-notification-grid/processing-notification-grid.component';
import { CommonModule } from '@angular/common';
import { TopBarComponent } from '../top-bar/top-bar.component';
import { ModalContainerComponent } from "../modal-container/modal-container.component";
import { MultiSelectComponent } from "../multi-select/multi-select.component";
import { BottomNavMenuComponent } from "../bottom-nav-menu/bottom-nav-menu.component";
import { LeftNavMenuComponent } from "../left-nav-menu/left-nav-menu.component";
import { OutsideClickDirective } from 'directives';
import { BehaviorSubject, debounceTime, map, Observable, tap } from 'rxjs';
import { BlurMaskComponent } from "../blur-mask/blur-mask.component";
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@Component({
  selector: 'app-side-nav-menu',
  imports: [RouterOutlet, NotificationGridComponent, ProcessingNotificationGridComponent, CommonModule, TopBarComponent, ModalContainerComponent, MultiSelectComponent, BottomNavMenuComponent, LeftNavMenuComponent, OutsideClickDirective, BlurMaskComponent],
  templateUrl: './side-nav-menu.component.html'
})
@UntilDestroy()
export class SideNavMenuComponent {
  protected readonly notifierService: NotifierService = inject(NotifierService);

  protected leftMenuOpenTrigger$ = new BehaviorSubject<boolean>(false);

  protected readonly leftMenuOpen$: Observable<'open' | 'close'>;

  protected readonly blurMaskService = inject(BlurMaskService);
  protected readonly router = inject(Router);

  constructor() {
    this.leftMenuOpen$ = this.leftMenuOpenTrigger$.pipe(
      debounceTime(100),
      map(open => open ? 'open' : 'close')
    );

    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.setLeftMenuOpen(false);
      }
    });
  }

  setLeftMenuOpen(value: boolean) {
    this.leftMenuOpenTrigger$.next(value);
    this.blurMaskService.setActive(value);
  }

}

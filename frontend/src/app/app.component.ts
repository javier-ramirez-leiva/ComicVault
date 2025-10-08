import { Component, inject, signal, WritableSignal } from '@angular/core';
import { SideNavMenuComponent, LoadingSpinnerPageComponent } from 'components';
import { DarkmodeService, AuthService } from 'services';
import { CommonModule } from '@angular/common';
import { delay, map, Observable } from 'rxjs';
@Component({
  selector: 'app-root',
  imports: [SideNavMenuComponent, CommonModule, LoadingSpinnerPageComponent],
  templateUrl: './app.component.html'
})
export class AppComponent {
  loggedIn: boolean | undefined = undefined;

  protected readonly darkModeService: DarkmodeService = inject(DarkmodeService);
  private readonly authService: AuthService = inject(AuthService);

  protected readonly initDone$: Observable<boolean>;

  constructor() {
    this.darkModeService.init();
    this.initDone$ = this.authService.sessionInit().pipe(map(result => true));
  }

}

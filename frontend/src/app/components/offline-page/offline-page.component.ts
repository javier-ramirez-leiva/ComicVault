import { ComicvaultLogoComponent } from '../comicvault-logo/comicvault-logo.component';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ActivePageService, AuthService } from 'services';

@UntilDestroy()
@Component({
  selector: 'app-offline-page',
  imports: [ComicvaultLogoComponent],
  templateUrl: './offline-page.component.html',
})
export class OfflinePageComponent {
  authService = inject(AuthService);
  activePageService = inject(ActivePageService);
  router = inject(Router);

  constructor() {
    this.authService
      .healthCheck()
      .pipe(untilDestroyed(this))
      .subscribe((healthy) => {
        if (healthy) {
          this.activePageService.activePage$.next('home');
          this.router.navigate(['/home']);
        }
      });
  }

}

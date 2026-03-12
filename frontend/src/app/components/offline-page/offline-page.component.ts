import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ActivePageService, AuthService, DarkmodeService } from 'services';

@UntilDestroy()
@Component({
  selector: 'app-offline-page',
  imports: [],
  templateUrl: './offline-page.component.html',
})
export class OfflinePageComponent implements OnInit {
  iconSrc: string = '';
  darkMode: DarkmodeService = inject(DarkmodeService);
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

  ngOnInit(): void {
    this.darkMode.isDarkMode$.pipe(untilDestroyed(this)).subscribe((dark) => {
      this.iconSrc = dark ? 'assets/icon-192x192.png' : 'assets/icon-light-192x192.png';
    });
  }
}

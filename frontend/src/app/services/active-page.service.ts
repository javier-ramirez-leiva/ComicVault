import { Injectable, inject } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BehaviorSubject } from 'rxjs';

const pages = [
  'login',
  'home',
  'library',
  'search',
  'downloads',
  'users',
  'devices',
  'settings',
  'user',
];

type Pages = (typeof pages)[number];

@Injectable({
  providedIn: 'root',
})
@UntilDestroy()
export class ActivePageService {
  activePage$: BehaviorSubject<Pages> = new BehaviorSubject<Pages>('home');
  activeRoot$: BehaviorSubject<Pages> = new BehaviorSubject<Pages>('home');
  isDetailsPage$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly router: Router = inject(Router);

  constructor() {
    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const rootURL = event.url.split('/')[1].split('?')[0];
        this.activeRoot$.next(rootURL);

        if (pages.includes(rootURL)) {
          this.isDetailsPage$.next(false);
          this.activePage$.next(rootURL);
        } else if (rootURL === 'comics' || rootURL === 'details') {
          this.isDetailsPage$.next(true);
          this.activePage$.next('library');
        } else if (rootURL === 'comics-search') {
          this.isDetailsPage$.next(true);
          this.activePage$.next('search');
        }
      }
    });
  }
}

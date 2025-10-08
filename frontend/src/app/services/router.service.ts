import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { resetRouteCache } from '../strategy_providers/custom-reuse-strategy';
import { ActivePageService } from './active-page.service';

@Injectable({
  providedIn: 'root'
})
export class RouterService {

  private readonly router = inject(Router);
  private readonly activePageService = inject(ActivePageService);

  constructor() { }

  reloadCurrentRoute(): void {
    if (this.activePageService.activePage$.value === 'library' || this.activePageService.activePage$.value === 'home') {
      resetRouteCache();
      const urlTree = this.router.parseUrl(this.router.url);
      const path = urlTree.root.children['primary']?.segments.map(it => it.path).join('/') || '';
      const queryParams = urlTree.queryParams;

      this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
        this.router.navigate(['/' + path], { queryParams });
      });
    }
  }

  reloadCurrentRouteForce(): void {

    const urlTree = this.router.parseUrl(this.router.url);
    const path = urlTree.root.children['primary']?.segments.map(it => it.path).join('/') || '';
    const queryParams = urlTree.queryParams;

    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
      this.router.navigate(['/' + path], { queryParams });
    });
  }
}

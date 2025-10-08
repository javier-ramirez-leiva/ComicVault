import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { LocalStorageService } from 'services';

export const libraryRedirectGuard: CanActivateFn = (): boolean | UrlTree => {
    const router = inject(Router);
    const localStorageService = inject(LocalStorageService);

    // Decide default based on localStorage
    const defaultView = localStorageService.getItem('contentSeries') ? 'series' : 'issues';

    // Redirect only when matching the empty child (i.e., /library)
    return router.createUrlTree(['/library', defaultView]);
};
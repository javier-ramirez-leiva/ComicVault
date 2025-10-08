import { CanActivateFn, Router } from '@angular/router';
import { AuthService, ActivePageService } from 'services';
import { inject } from '@angular/core';
import { allowRegisterAccess } from './register.guard';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const activePageService = inject(ActivePageService);
  const router = inject(Router);

  if (authService.loggedIn()) {
    // User is already logged in
    return of(true);
  }

  return authService.sessionInit().pipe(
    switchMap((isAuthenticated: boolean) => {
      if (isAuthenticated) {
        return of(true);
      } else {
        // If not authenticated, check if admin exists
        return authService.adminUserExists().pipe(
          catchError(error => of(false)),
          map((exists) => {
            if (exists) {
              activePageService.activePage$.next('login');
              return router.createUrlTree(['/login']);
            } else {
              allowRegisterAccess();
              activePageService.activePage$.next('register');
              return router.createUrlTree(['/register']);
            }
          })
        );
      }
    })
  );
};
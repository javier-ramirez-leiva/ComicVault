import { CanActivateFn, Router } from '@angular/router';
import { AuthService, ActivePageService } from 'services';
import { inject } from '@angular/core';
import { allowRegisterAccess } from './register.guard';
import { catchError, EMPTY, of } from 'rxjs';

export const loginGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const activePageService = inject(ActivePageService);
  const router = inject(Router);

  authService
    .adminUserExists()
    .pipe(
      catchError((error) => {
        activePageService.activePage$.next('offline');
        router.navigate(['/offline']);
        return EMPTY;
      }),
    )
    .subscribe((exists: Boolean) => {
      if (!exists) {
        allowRegisterAccess();
        activePageService.activePage$.next('register');
        router.navigate(['/register']);
      }
    });
  return true;
};

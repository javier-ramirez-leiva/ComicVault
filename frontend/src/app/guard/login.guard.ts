import { CanActivateFn, Router } from '@angular/router';
import { AuthService, ActivePageService } from 'services';
import { inject } from '@angular/core';
import { allowRegisterAccess } from './register.guard';
import { catchError, of } from 'rxjs';

export const loginGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const activePageService = inject(ActivePageService);
  const router = inject(Router);

  authService.adminUserExists().pipe(
    catchError(error => of(false))
  ).subscribe((exists: Boolean) => {
    if (!exists) {
      allowRegisterAccess();
      activePageService.activePage$.next('register');
      router.navigate(['/register']);
    }
  });
  return true;
};
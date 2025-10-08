import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { Router } from '@angular/router';

let canAccessRegister = false;

export const registerGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  if (canAccessRegister) {
    canAccessRegister = false;
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};

export function allowRegisterAccess() {
  canAccessRegister = true;
}

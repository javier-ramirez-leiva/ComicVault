import { HttpInterceptorFn } from '@angular/common/http';
import { AuthService } from 'services';
import { inject } from '@angular/core';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const excludedPaths = [
    '/users/authenticate',
    '/users/registerFirstAdmin',
    '/users/adminUserExists',
    '/health',
    '/cover',
  ];
  const isExclude = excludedPaths.some((path) => req.url.includes(path));
  if (isExclude) {
    return next(req);
  }

  const authService = inject(AuthService);

  // Always send cookies for all requests
  const clonedReq = req.clone({
    withCredentials: true,
  });

  return next(clonedReq);
};

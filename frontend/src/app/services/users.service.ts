import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { HttpResponseError, RegisterRequest } from 'interfaces';
import { UserInfoResponse } from 'interfaces';
import { HttpService } from './http.service';
import { NotifierService } from './notifier.service';

@Injectable({
  providedIn: 'root'
})
export class UsersService {

  private readonly httpService = inject(HttpService);
  private readonly notifier = inject(NotifierService);

  allUsers(): Observable<UserInfoResponse[]> {
    return this.httpService.request<UserInfoResponse[]>(
      'GET',
      `/users`,
    );
  }

  getMeUser(): Observable<UserInfoResponse> {
    return this.httpService.request<UserInfoResponse>(
      'GET',
      `/users/me`,
    );
  }


  registerFirstAdmin(registerRequest: RegisterRequest): Observable<Boolean> {
    return this.httpService.request<Boolean>(
      'POST',
      `/users/registerFirstAdmin`,
      registerRequest,
    );
  }

  registerUser(registerRequest: RegisterRequest): Observable<any> {
    return this.httpService.request<any>(
      'POST',
      `/users/register`,
      registerRequest,
    ).pipe(
      catchError((errorResponse) => {
        const error: HttpResponseError = errorResponse.error;
        let errorMessage = 'An unexpected error occurred.';
        if (error.errorCode === 'ONE_ADMIN' || error.errorCode === 'RESOURCE_ALREADY_EXISTING_FOUND') {
          errorMessage = error.message;
          this.notifier.appendNotification({
            id: 0,
            title: 'Error',
            message: error.message,
            type: 'error',
          });
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  deleteUser(username: String): Observable<any> {

    return this.httpService.request<any>(
      'DELETE',
      `/users/${username}`,
    ).pipe(
      catchError((errorResponse) => {
        const error: HttpResponseError = errorResponse.error;
        let errorMessage = 'An unexpected error occurred.';
        if (error.errorCode === 'ONE_ADMIN') {
          errorMessage = error.message;
          this.notifier.appendNotification({
            id: 0,
            title: 'Error',
            message: error.message,
            type: 'error',
          });
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  editUser(username: String, registerRequest: RegisterRequest): Observable<any> {
    return this.httpService.request<any>(
      'PUT',
      `/users/${username}`,
      registerRequest,
    ).pipe(
      catchError((errorResponse) => {
        const error: HttpResponseError = errorResponse.error;
        let errorMessage = 'An unexpected error occurred.';
        if (error.errorCode === 'ONE_ADMIN') {
          errorMessage = error.message;
          this.notifier.appendNotification({
            id: 0,
            title: 'Error',
            message: error.message,
            type: 'error',
          });
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  editMeUser(registerRequest: RegisterRequest): Observable<any> {
    return this.httpService.request<any>(
      'PUT',
      `/users/me`,
      registerRequest,
    ).pipe(
      catchError((errorResponse) => {
        const error: HttpResponseError = errorResponse.error;
        let errorMessage = 'An unexpected error occurred.';
        if (error.errorCode === 'ONE_ADMIN') {
          errorMessage = error.message;
          this.notifier.appendNotification({
            id: 0,
            title: 'Error',
            message: error.message,
            type: 'error',
          });
        }
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}

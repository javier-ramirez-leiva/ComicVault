import { Injectable, inject, isDevMode, signal } from '@angular/core';
import { AuthentificationRequest, AuthentificationResponse, Role, UserInfoResponse, isHttpResponseError } from 'interfaces';
import { EMPTY, Observable, ReplaySubject, Subject, catchError, delay, filter, map, of, switchMap, tap, throwError } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { NotifierService } from './notifier.service';
import { Router } from '@angular/router';
import { ActivePageService } from './active-page.service';
import { CookieService } from 'ngx-cookie-service';
import { allowRegisterAccess } from '../guard/register.guard';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ConfigURLService } from './configURL.service';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
@UntilDestroy()
export class AuthService {

  loggedIn = signal<boolean>(false);
  private readonly configURLService = inject(ConfigURLService);
  private readonly httpClient: HttpClient = inject(HttpClient);
  private readonly notifier: NotifierService = inject(NotifierService);
  private readonly router: Router = inject(Router);
  private readonly activePageService: ActivePageService = inject(ActivePageService);
  private readonly cookieService: CookieService = inject(CookieService);

  private username: string = '';
  private role: Role = Role.Viewer;
  private user: UserInfoResponse = {
    username: '',
    role: Role.Viewer,
    color: ''
  };
  private color: string = '';
  username$: ReplaySubject<string> = new ReplaySubject<string>(1);
  role$: ReplaySubject<Role> = new ReplaySubject<Role>(1);
  color$: ReplaySubject<string> = new ReplaySubject<string>(1);
  user$: ReplaySubject<UserInfoResponse> = new ReplaySubject<UserInfoResponse>(1);

  constructor() { }

  sessionInit(): Observable<boolean> {

    //If strings are not empty, set the authentification

    return this.session().pipe(
      switchMap((userInfo) => {
        this.user = userInfo;
        this.user$.next(userInfo);
        this.setAuthentification({
          username: userInfo.username,
          role: userInfo.role,
          color: userInfo.color
        });
        return of(true); // Return true if successful
      }),
      catchError((error) => {
        this.errorNotification(error);
        this.signOut();
        return of(false); // Return false if there's an error

      })
    );
  }


  setAuthentification(authentification: AuthentificationResponse): void {
    this.loggedIn.set(true);
    this.user$.next({
      username: authentification.username,
      role: authentification.role,
      color: authentification.color
    });
    this.username$.next(authentification.username);
    this.role$.next(authentification.role);
    this.color$.next(authentification.color);
    this.username = authentification.username;
    this.role = authentification.role;
    this.color = authentification.color;
  }

  signOut() {
    this.loggedIn.set(false);
    this.forgetMe().pipe(untilDestroyed(this)).subscribe();

  }


  handleApiCallError<T>(apiCall: () => Observable<T>): Observable<T> {
    let tries = 0;
    return apiCall().pipe(
      catchError((error) => {
        if (error.status === 401 || error.status === 403) {
          return this.refresh().pipe(
            catchError(error => {
              if (error.status === 401 || error.status === 403 || error.status === 404) {
                /*return this.adminUserExists().pipe(
                  untilDestroyed(this),
                  catchError(error => {
                    return of(false);
                  }),
                  map((response) => {
                    if (response) {
                      this.errorNotification(error);
                      this.activePageService.activePage$.next('login');
                      this.router.navigate(['/login']);
                      return of(error);
                    } else {
                      allowRegisterAccess();
                      this.activePageService.activePage$.next('register');
                      this.router.navigate(['/register']);
                      return of(error);
                    }
                  }),
                );*/
                this.errorNotification(error);
                this.activePageService.activePage$.next('login');
                this.router.navigate(['/login']);
                return of(error);
              }
              return throwError(() => error);
            }),
            switchMap(() => {
              // Set authentication with new access token
              this.setAuthentification({
                username: this.username,
                role: this.role,
                color: this.color,
              });
              // Retry the original API call with the new access token up to 5 times max
              console.log('Retry');
              if (tries >= 5) {
                return EMPTY;
              }
              tries++;
              return apiCall();
            })
          );
        }
        else {
          return this.healthCheck().pipe(
            switchMap((isHealthy) => {
              if (isHealthy) {
                return throwError(() => error); // Propagate the original error if the server is healthy
              } else {
                return throwError(() => new Error("Server is unhealthy")); // Return a new error if the server is unhealthy
              }
            })
          );
        }
      }),
      catchError((error) => {
        if (environment.logTraceEnabled && isHttpResponseError(error.error)) {
          this.notifier.appendNotification({
            id: 0,
            title: error.error.errorCode,
            message: JSON.stringify(error.error.message),
            type: 'debug'
          });
        }
        return throwError(() => error);
      })
    );
  }

  errorNotification(error: any) {
    if (isDevMode()) {
      this.notifier.appendNotification({
        id: 0,
        title: 'Error!',
        message: JSON.stringify(error),
        type: 'error'
      });
    }
  }

  healthCheck(): Observable<boolean> {
    return this.httpClient.get<any>(`${this.configURLService.baseURL}/${this.configURLService.apiVersion}/health`).pipe(
      catchError(() => {
        return of(false);
      }),
      switchMap((data) => {
        if (data && data.status === "healthy") {
          return of(true);
        } else {
          this.healthNotification();
          return of(false);
        }
      })
    );
  }

  healthNotification() {
    this.notifier.appendNotification({
      id: 0,
      title: 'Error!',
      message: 'Error health check of the server',
      type: 'error'
    });
  }

  session(): Observable<UserInfoResponse> {
    return this.handleApiCallError(() => {
      return this.httpClient.get<UserInfoResponse>(`${this.configURLService.baseURL}/${this.configURLService.apiVersion}/users/session`);
    });
  }

  refresh(): Observable<any> {
    return this.httpClient.get<any>(
      `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/users/refresh`,
    ).pipe(
      /*catchError(error => {
        this.notifier.appendNotification({
          id: 0,
          title: 'Error Refrehs token!',
          message: JSON.stringify(error),
          type: 'error'
        });
        return of(error);
      }),
      tap(() => this.notifier.appendNotification({
        id: 0,
        title: 'Refresh token refreshed!',
        message: '',
        type: 'success'
      }))*/
    );
  }

  forgetMe(): Observable<any> {
    return this.httpClient.get<any>(
      `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/users/forgetMe`
    );
  }

  adminUserExists(): Observable<Boolean> {
    return this.handleApiCallError(() => {
      return this.httpClient.get<Boolean>(`${this.configURLService.baseURL}/${this.configURLService.apiVersion}/users/adminUserExists`);
    });
  }

  authenticate(authentificationRequest: AuthentificationRequest): Observable<AuthentificationResponse> {
    return this.handleApiCallError(() => {
      return this.httpClient.post<AuthentificationResponse>(`${this.configURLService.baseURL}/${this.configURLService.apiVersion}/users/authenticate`, authentificationRequest);
    });
  }

  getUsername(): string {
    return this.username;
  }

  getRole(): Role {
    return this.role;
  }

  getUser(): UserInfoResponse {
    return this.user;
  }

}

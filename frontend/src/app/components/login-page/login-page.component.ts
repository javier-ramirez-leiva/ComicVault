import { Component, OnInit, inject } from '@angular/core';
import { AuthService } from 'services';

import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotifierService } from 'services';
import { DarkmodeService } from 'services';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { DeviceDetectorService } from 'ngx-device-detector';
import { InputTextComponent } from '../input-text/input-text.component';

@UntilDestroy()
@Component({
  selector: 'app-login-page',
  imports: [FormsModule, InputTextComponent],
  templateUrl: './login-page.component.html',
})
export class LoginPageComponent implements OnInit {
  authService: AuthService = inject(AuthService);
  username: string = '';
  device: string = '';
  password: string = '';
  router: Router = inject(Router);
  notifier: NotifierService = inject(NotifierService);
  darkMode: DarkmodeService = inject(DarkmodeService);
  deviceDetectorService: DeviceDetectorService = inject(DeviceDetectorService);
  iconSrc: string =
    this.darkMode.darkModeSignal() === 'dark'
      ? 'assets/icon-192x192.png'
      : 'assets/icon-192x192.png';

  ngOnInit(): void {
    this.authService.signOut();
    const deviceInfo = this.deviceDetectorService.getDeviceInfo();
    this.device = deviceInfo.device;
  }

  login() {
    const deviceInfo = this.deviceDetectorService.getDeviceInfo();
    const authData = {
      username: this.username,
      password: this.password,
      userAgent: deviceInfo.userAgent,
      os: deviceInfo.os,
      browser: deviceInfo.browser,
      device: this.device,
      osVersion: deviceInfo.os_version,
      browserVersion: deviceInfo.browser_version,
      orientation: deviceInfo.orientation,
    };
    this.authService
      .authenticate(authData)
      .pipe(
        catchError((error) => {
          this.notifier.appendNotification({
            id: 0,
            title: 'Login Error!',
            message: 'Username and password are incorrect',
            type: 'error',
          });
          return throwError(() => error);
        }),
        untilDestroyed(this),
      )
      .subscribe((data) => {
        if (data) {
          this.authService.setAuthentification(data);
          this.router.navigate(['/home']);
        }
      });
  }
}

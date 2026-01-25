import { Component, OnInit, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Role } from 'interfaces';
import { UsersService, AuthService, NotifierService, DarkmodeService } from 'services';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { DeviceDetectorService } from 'ngx-device-detector';
import { InputTextComponent } from '../input-text/input-text.component';

@UntilDestroy()
@Component({
  selector: 'app-register-page',
  imports: [FormsModule, InputTextComponent],
  templateUrl: './register-page.component.html',
})
export class RegisterPageComponent implements OnInit {
  username: string = '';
  device: string = '';
  password: string = '';
  userservice: UsersService = inject(UsersService);
  authService: AuthService = inject(AuthService);
  notifier: NotifierService = inject(NotifierService);
  router: Router = inject(Router);
  darkMode: DarkmodeService = inject(DarkmodeService);
  deviceDetectorService: DeviceDetectorService = inject(DeviceDetectorService);
  iconSrc: string =
    this.darkMode.darkModeSignal() === 'dark'
      ? 'assets/icon-192x192.png'
      : 'assets/icon-192x192.png';

  ngOnInit(): void {
    const deviceInfo = this.deviceDetectorService.getDeviceInfo();
    this.device = deviceInfo.device;
  }

  register() {
    const registerData = {
      username: this.username,
      password: this.password,
      role: Role.Admin,
      color: '#2563eb',
    };
    this.userservice.registerFirstAdmin(registerData).subscribe(() => {
      this.login();
    });
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

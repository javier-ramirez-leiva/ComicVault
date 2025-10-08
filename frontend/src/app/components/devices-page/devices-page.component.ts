import { Component, inject } from '@angular/core';
import { DevicesService } from 'services';
import { BehaviorSubject, Observable, combineLatest, filter, map, switchMap } from 'rxjs';
import { DeviceInfo, UserInfoResponse } from 'interfaces';
import { UsersService } from 'services';
import { CommonModule } from '@angular/common';
import { ModalService } from 'services';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ModalDeviceInfoComponent } from '../modal-device-info/modal-device-info.component';
import { AvatarUserComponent } from "../avatar-user/avatar-user.component";
import { ModalMessageComponent } from '../modal-message/modal-message.component';

@UntilDestroy()
@Component({
  selector: 'app-devices-page',
  imports: [CommonModule, AvatarUserComponent],
  templateUrl: './devices-page.component.html'
})
export class DevicesPageComponent {

  private readonly devicesService = inject(DevicesService);
  private readonly usersService = inject(UsersService);
  private readonly modalService = inject(ModalService);
  private readonly triggerUpdate$ = new BehaviorSubject<void>(undefined);

  private readonly devices$: Observable<DeviceInfo[]> = this.triggerUpdate$.pipe(
    switchMap(() => this.devicesService.allDevices())
  )

  protected readonly devicesAndUsers$: Observable<DeviceInfoAndUser[]> = combineLatest([
    this.devices$,
    this.usersService.allUsers(),
    this.triggerUpdate$
  ]).pipe(
    map(([devices, users, _]) => {
      return devices.map(device => {
        const user = users.find(user => user.username === device.username);
        return {
          deviceInfo: device,
          user: user
        };
      });
    })
  )

  trackByDeviceUser(index: number, devicesAndUsers: DeviceInfoAndUser): string {
    return devicesAndUsers.deviceInfo.id.toString();
  }

  deleteDeviceId(devicesAndUser: DeviceInfoAndUser) {

    const message = `Are you sure you want to delete ${devicesAndUser.deviceInfo.device} device for ${devicesAndUser.user?.username}? Login will be required.`;
    this.modalService.open<boolean, { message: string | undefined }>(ModalMessageComponent, { message: message }).pipe(
      filter(response => response === true),
      switchMap(_ => {
        return this.devicesService.deleteDevice(devicesAndUser.deviceInfo.id);
      }),
      untilDestroyed(this)
    ).subscribe(response => {
      this.triggerUpdate$.next();
    });
  }

  displayDetails(device: DeviceInfo) {
    this.modalService.open<null, { device: DeviceInfo | undefined }>(ModalDeviceInfoComponent, { device: device }).pipe(
      untilDestroyed(this)
    ).subscribe();
  }
}

type DeviceInfoAndUser = {
  deviceInfo: DeviceInfo;
  user: UserInfoResponse | undefined;
}

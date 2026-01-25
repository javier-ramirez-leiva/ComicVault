import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable, filter } from 'rxjs';
import { RegisterRequest, Role, UserInfoResponse } from 'interfaces';
import { UsersService, NotifierService, ModalService } from 'services';
import {
  ModalUsersComponent,
  ModalUsersComponentInput,
} from '../modal-users/modal-users.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ModalMessageComponent } from '../modal-message/modal-message.component';
import { RouterModule } from '@angular/router';
import { AvatarUserComponent } from '../avatar-user/avatar-user.component';

@Component({
  selector: 'app-users-page',
  imports: [CommonModule, RouterModule, AvatarUserComponent],
  templateUrl: './users-page.component.html',
})
@UntilDestroy()
export class UsersPageComponent {
  users$!: Observable<UserInfoResponse[]>;
  usersService = inject(UsersService);
  private modalService: ModalService = inject(ModalService);
  userRegister: RegisterRequest = {
    username: '',
    password: '',
    role: Role.Viewer,
    color: '#2563eb',
  };
  modifyUser: boolean = false;
  modifyRole: boolean = false;

  userService = inject(UsersService);
  notifierService: NotifierService = inject(NotifierService);

  constructor() {
    this.users$ = this.usersService.allUsers();
  }

  createUser() {
    this.userRegister = {
      username: '',
      password: '',
      role: Role.Viewer,
      color: '#2563eb',
    };
    this.modifyUser = true;
    this.modifyRole = true;
    const input: ModalUsersComponentInput = {
      modifyUser: this.modifyUser,
      modifyRole: this.modifyRole,
      user: this.userRegister,
    };
    this.triggerModal(input);
  }
  editUser(user: UserInfoResponse) {
    this.userRegister = {
      username: user.username,
      password: '',
      role: user.role,
      color: user.color,
    };
    this.modifyUser = false;
    this.modifyRole = true;
    const input: ModalUsersComponentInput = {
      modifyUser: this.modifyUser,
      modifyRole: this.modifyRole,
      user: this.userRegister,
    };
    this.triggerModal(input);
  }

  triggerModal(input: ModalUsersComponentInput): void {
    this.modalService
      .open<RegisterRequest, { input: ModalUsersComponentInput | undefined }>(ModalUsersComponent, {
        input: input,
      })
      .pipe(
        filter((response) => response !== undefined),
        untilDestroyed(this),
      )
      .subscribe((response) => {
        if (this.modifyUser) {
          this.userService
            .registerUser(this.userRegister)
            .pipe(untilDestroyed(this))
            .subscribe((response) => {
              this.notifierService.appendNotification({
                id: 0,
                title: 'Success',
                message: 'User created!',
                type: 'success',
              });
              this.users$ = this.usersService.allUsers();
            });
        } else {
          this.userService
            .editUser(this.userRegister.username, this.userRegister)
            .pipe(untilDestroyed(this))
            .subscribe((response) => {
              this.notifierService.appendNotification({
                id: 0,
                title: 'Success',
                message: 'User updated!',
                type: 'success',
              });
              this.users$ = this.usersService.allUsers();
            });
        }
      });
  }

  deleteUser(user: UserInfoResponse) {
    const message = `Are you sure you want to delete the user: ${user.username}? Read status of the user will be deleted`;
    this.modalService
      .open<boolean, { message: string | undefined }>(ModalMessageComponent, { message: message })
      .pipe(
        filter((response) => response === true),
        untilDestroyed(this),
      )
      .subscribe((response) => {
        this.userService
          .deleteUser(user.username)
          .pipe(untilDestroyed(this))
          .subscribe((response) => {
            this.notifierService.appendNotification({
              id: 0,
              title: 'Success',
              message: 'User deleted!',
              type: 'warning',
            });
            this.users$ = this.usersService.allUsers();
          });
      });
  }

  trackByUsers(index: number, user: UserInfoResponse): string {
    return user.username;
  }
}

import { Component, Input, OnChanges, OnInit, SimpleChanges, inject } from '@angular/core';
import { RegisterRequest, Role } from 'interfaces';
import { ModalComponent } from 'interfaces';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { filter } from 'rxjs';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { NotifierService } from 'services';

import { InputTextComponent } from '../input-text/input-text.component';
import { CrossModalComponent } from '../cross-modal/cross-modal.component';
import { InputColorComponent } from '../input-color/input-color.component';

export type ModalUsersComponentInput = {
  modifyUser: boolean;
  modifyRole: boolean;
  user: RegisterRequest;
};

@UntilDestroy()
@Component({
  selector: 'app-modal-users',
  imports: [
    ReactiveFormsModule,
    FormsModule,
    InputTextComponent,
    CrossModalComponent,
    InputColorComponent,
  ],
  templateUrl: './modal-users.component.html',
})
export class ModalUsersComponent
  implements
    ModalComponent<RegisterRequest, { input: ModalUsersComponentInput | undefined }>,
    OnInit,
    OnChanges
{
  @Input({ required: true }) data?: { input: ModalUsersComponentInput | undefined };
  protected user!: RegisterRequest;
  roleControl: FormControl<Role | null> = new FormControl(null);
  private readonly notifierService = inject(NotifierService);

  close!: (response?: RegisterRequest) => void;

  ngOnInit(): void {
    this.user = this.data?.input?.user ?? {
      username: '',
      password: '',
      role: Role.Viewer,
      color: '#2563eb',
    };
    this.roleControl.setValue(this.user.role);

    this.roleControl.valueChanges
      .pipe(
        filter((value) => value !== null && value !== undefined),
        untilDestroyed(this),
      )
      .subscribe((value: Role | null) => {
        if (value !== null) {
          this.user.role = value;
        }
      });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['user']) {
      this.roleControl.setValue(this.user.role);
    }
  }

  cancelUser() {
    this.close(undefined);
  }
  okUser() {
    if (this.user.username === '') {
      this.notifierService.appendNotification({
        id: 0,
        title: 'Error!',
        message: 'Username cannot be empty',
        type: 'error',
      });
      return;
    } else if (this.user.username === 'me') {
      this.notifierService.appendNotification({
        id: 0,
        title: 'Error!',
        message: 'Username cannot be "me"',
        type: 'error',
      });
      return;
    } else if (this.user.password === '') {
      this.notifierService.appendNotification({
        id: 0,
        title: 'Error!',
        message: 'Password cannot be empty',
        type: 'error',
      });
      return;
    }
    this.close(this.user);
  }
}

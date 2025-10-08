import { Component, OnInit, inject } from '@angular/core';
import { HistoryService, AuthService, UsersService, ModalService, NotifierService, TopBarService } from 'services';
import { Observable, filter, map, switchMap, tap } from 'rxjs';
import { HistoryTableComponent } from '../history-table/history-table.component';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { History } from 'interfaces';
import { RegisterRequest, UserInfoResponse } from 'interfaces';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ModalUsersComponent, ModalUsersComponentInput } from '../modal-users/modal-users.component';
import { FilterHistory } from 'interfaces';
import { NoResultsComponent } from "../no-results/no-results.component";
import { FunnelButtonHistoryComponent } from '../funnel-button-history/funnel-button-history.component';

@Component({
  selector: 'app-user-me',
  imports: [HistoryTableComponent, CommonModule, RouterModule, NoResultsComponent, FunnelButtonHistoryComponent],
  templateUrl: './user-page.component.html'
})

@UntilDestroy()
export class UserPageComponent implements OnInit {

  private readonly historyService = inject(HistoryService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UsersService);
  private readonly modalService = inject(ModalService);
  private readonly notifierService = inject(NotifierService);
  private readonly topBarService = inject(TopBarService);
  protected histories$: Observable<History[]> = new Observable<History[]>();
  protected user$: Observable<UserInfoResponse> = new Observable<UserInfoResponse>();
  private userRegister!: RegisterRequest;
  private userParamID!: string;
  private filterHistory: FilterHistory = {
    comicTitle: '',
    dateEnd: '',
    dateStart: '',
    inLibraryNo: true,
    inLibraryYes: true,
    readStatusOnGoing: true,
    readStatusRead: true
  }


  constructor() {

  }

  ngOnInit(): void {
    this.userParamID = this.route.snapshot.params['username'];
    this.updateUserStream();

    this.user$.pipe(untilDestroyed(this)).subscribe(user => {
      this.userRegister = {
        username: user.username,
        password: "",
        role: user.role,
        color: user.color
      };
    });

    this.route.queryParams.pipe(untilDestroyed(this)).subscribe((data) => {
      if (Object.keys(data).length > 0) {
        this.filterHistory = data as FilterHistory;
        this.updateUserStream();
      }

    });

    this.filterHistory = this.getHistoryFilter();

    this.topBarService.setFilterHistoryFormValue(this.filterHistory);
  }

  updateUserStream() {
    if (this.userParamID) {
      this.user$ = this.userService.allUsers().pipe(
        map(users => users.find(user => user.username === this.userParamID)),
        filter(notNullOrUndefined())
      );
      this.histories$ = this.user$.pipe(
        switchMap(user => this.historyService.historyUsername(user.username, this.filterHistory))
      );
    } else {
      this.user$ = this.userService.getMeUser();
      this.histories$ = this.user$.pipe(
        switchMap(user => this.historyService.historyMe(this.filterHistory))
      );
    }
  }

  getHistoryFilter(): FilterHistory {
    const queryParams = this.route.snapshot.queryParams;
    return {
      comicTitle: queryParams['comicTitle'] || '',
      dateStart: queryParams['dateStart'] || '',
      dateEnd: queryParams['dateEnd'] || '',
      inLibraryNo: queryParams['inLibraryNo'] === 'false' ? false : true,
      inLibraryYes: queryParams['inLibraryYes'] === 'false' ? false : true,
      readStatusOnGoing: queryParams['readStatusOnGoing'] === 'false' ? false : true,
      readStatusRead: queryParams['readStatusRead'] === 'false' ? false : true,
    };
  }


  editUser() {
    const input: ModalUsersComponentInput = {
      modifyUser: false,
      modifyRole: this.userParamID ? true : false,
      user: this.userRegister
    }
    this.triggerModal(input);
  }

  triggerModal(input: ModalUsersComponentInput): void {
    this.modalService.open<RegisterRequest, { input: ModalUsersComponentInput | undefined }>(ModalUsersComponent, { input: input }).pipe(
      filter(response => response !== undefined),
      untilDestroyed(this)
    ).subscribe(response => {
      let userObs$: Observable<UserInfoResponse> = new Observable<UserInfoResponse>();
      if (this.userParamID) {
        userObs$ = this.userService.editUser(this.userRegister.username, this.userRegister);
      } else {
        userObs$ = this.userService.editMeUser(this.userRegister);
      }
      userObs$.pipe(
        /*Trigger other updates*/
        switchMap(_ => this.authService.sessionInit()),
        tap(() => this.updateUserStream()),
        untilDestroyed(this)
      ).subscribe((_) => {
        this.notifierService.appendNotification({
          id: 0,
          title: 'Success',
          message: 'User updated!',
          type: 'success'
        });
      });
    })
  };

}

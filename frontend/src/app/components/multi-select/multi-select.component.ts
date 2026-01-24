import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { CoverCardClickCollectorService, ModalService } from 'services';
import { MultiSelectService } from 'services';
import { ComicsService } from 'services';
import { NotifierService } from 'services';
import { HideRolesDirective } from 'directives';
import { Role } from 'interfaces';
import { DownloadService } from 'services';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';
import { NavigationEnd, Router } from '@angular/router';
import { ModalMessageComponent } from '../modal-message/modal-message.component';
import { filter, switchMap, tap } from 'rxjs';
import { ModalMessageListComponent } from '../modal-message-list/modal-message-list.component';

@Component({
  selector: 'app-multi-select',
  imports: [CommonModule, HideRolesDirective],
  templateUrl: './multi-select.component.html',
})
@UntilDestroy()
export class MultiSelectComponent implements OnInit {
  protected readonly coverCardClickCollectorService = inject(CoverCardClickCollectorService);
  protected readonly multiSelectService = inject(MultiSelectService);
  private readonly comicService = inject(ComicsService);
  private readonly downloadService = inject(DownloadService);
  private readonly notifier = inject(NotifierService);
  private readonly router = inject(Router);
  private readonly modalService = inject(ModalService);

  protected activated: boolean = false;
  protected displayDropdown: boolean = false;

  protected displayed = false;

  Role = Role;

  ngOnInit(): void {
    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const routeString = event.urlAfterRedirects;
        this.displayed =
          routeString.startsWith('/series') || routeString.startsWith('/library/issues');
        if (!this.displayed) {
          this.displayDropdown = false;
          this.coverCardClickCollectorService.clearActiveHovers();
          this.activated = false;
          this.coverCardClickCollectorService.setMultiSelect(false);
        }
      }
    });
  }

  protected toggleActivation(): void {
    if (!this.activated) {
      this.activated = true;
      this.displayDropdown = true;
      this.coverCardClickCollectorService.setMultiSelect(true);
    } else {
      this.activated = false;
      this.displayDropdown = false;
      this.coverCardClickCollectorService.setMultiSelect(false);
    }
  }

  markAsRead(value: boolean): void {
    const comicIDs = this.coverCardClickCollectorService
      .getActiveCards()
      .map((card) => card.getComic().id);
    const listTitles = this.coverCardClickCollectorService
      .getActiveCards()
      .map((card) => card.getComic().title);
    const message = `Are you sure you want to mark ${comicIDs.length} comics as ${value ? 'read' : 'not read'}?`;
    this.toggleActivation();
    this.modalService
      .open<boolean, { message: string; list: string[] }>(ModalMessageListComponent, {
        message: message,
        list: listTitles,
      })
      .pipe(
        filter((response) => response === true),
        switchMap((_) => this.comicService.setComicListReadStatus(comicIDs, value)),
      )
      .pipe(untilDestroyed(this))
      .subscribe(() => {
        this.notifier.appendNotification({
          id: 0,
          title: 'Comics updated',
          message: `${comicIDs.length} comics marked as ${value ? 'read' : 'not read'}`,
          type: 'success',
        });
        this.multiSelectService.refresh();
      });
    this.coverCardClickCollectorService.setMultiSelect(false);
    resetRouteCache();
  }

  delete(): void {
    const comicIDs = this.coverCardClickCollectorService
      .getActiveCards()
      .map((card) => card.getComic().id);
    const listTitles = this.coverCardClickCollectorService
      .getActiveCards()
      .map((card) => card.getComic().title);
    const message = `Are you sure you want to delete ${comicIDs.length} comics?`;
    this.toggleActivation();
    this.modalService
      .open<boolean, { message: string; list: string[] }>(ModalMessageListComponent, {
        message: message,
        list: listTitles,
      })
      .pipe(
        switchMap((_) => this.comicService.deleteComicList(comicIDs)),
        untilDestroyed(this),
      )
      .pipe(untilDestroyed(this))
      .subscribe(() => {
        this.notifier.appendNotification({
          id: 0,
          title: 'Comics deleted',
          message: `${comicIDs.length} comics deleted'}`,
          type: 'warning',
        });
        this.multiSelectService.refresh();
        resetRouteCache();
      });
  }

  download(): void {
    const comics = this.coverCardClickCollectorService
      .getActiveCards()
      .map((card) => card.getComic())
      .filter(notNullOrUndefined());
    this.notifier.appendNotification({
      id: 0,
      title: 'Download on device launched',
      message: `${comics.length} comics downloading`,
      type: 'info',
    });
    this.downloadService.downloadComicList(comics);
    this.coverCardClickCollectorService.setMultiSelect(false);
    this.multiSelectService.refresh();
    this.toggleActivation();
  }
}

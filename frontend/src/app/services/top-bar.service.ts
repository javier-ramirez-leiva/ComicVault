import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, ReplaySubject, Subject } from 'rxjs';
import { FilterFunnel } from 'components';
import { FilterHistory, JobFilter } from 'interfaces';
import { NavigationEnd, Router } from '@angular/router';
import { ActivePageService } from './active-page.service';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { AdvanceFilterFunnel } from '../components/advance-funnel-button/advance-funnel-button.component';

@UntilDestroy()
@Injectable({
  providedIn: 'root',
})
export class TopBarService {
  searchTextChangeLibEvent$: ReplaySubject<string> = new ReplaySubject<string>();
  filterHistoryFormEvent$: ReplaySubject<FilterHistory> = new ReplaySubject<FilterHistory>();
  resetFilterFunnelFormEvent$: Subject<void> = new Subject<void>();
  resetAdvancedFilterFunnelFormEvent$: Subject<void> = new Subject<void>();
  resetJobFilterFunnelFormEvent$: Subject<void> = new Subject<void>();

  filterEvent$: ReplaySubject<FilterFunnel> = new ReplaySubject<FilterFunnel>();
  advanceFilterEvent$: ReplaySubject<AdvanceFilterFunnel> =
    new ReplaySubject<AdvanceFilterFunnel>();
  jobFilterEvent$: ReplaySubject<JobFilter> = new ReplaySubject<JobFilter>();
  contentChange$: ReplaySubject<boolean> = new ReplaySubject<boolean>();
  searchBarTextModifierLib$: Subject<string> = new Subject<string>();
  searchBarTextModifierSearch$: Subject<string> = new Subject<string>();

  drawerOpen$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  private readonly activePageService = inject(ActivePageService);
  private readonly router = inject(Router);

  constructor() {
    this.activePageService.activePage$.pipe(untilDestroyed(this)).subscribe((activePage) => {
      if (activePage !== 'library') {
        this.setSearchBarTextLib('');
        this.resetFilterFunnel();
      }
      if (activePage !== 'advanceLibrary') {
        this.setSearchBarTextLib('');
        this.resetAdvancedFilterFunnel();
      }
    });

    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        if (!event.url.startsWith('/settings/jobs')) {
          this.resetJobFilterFunnel();
        }
      }
    });
  }

  emitTextChangeLibEvent(text: string) {
    this.searchTextChangeLibEvent$.next(text);
  }

  emitFilterChangeEvent(filter: FilterFunnel) {
    this.filterEvent$.next(filter);
  }

  emitAdvanceFilterChangeEvent(filter: AdvanceFilterFunnel) {
    this.advanceFilterEvent$.next(filter);
  }

  emitJobFilterChangeEvent(filter: JobFilter) {
    this.jobFilterEvent$.next(filter);
  }

  emitContentChangeEvent(content: boolean) {
    this.contentChange$.next(content);
  }

  setSearchBarTextLib(text: string) {
    this.searchBarTextModifierLib$.next(text);
    this.searchTextChangeLibEvent$.next(text);
  }

  resetFilterFunnel() {
    this.resetFilterFunnelFormEvent$.next();
  }

  resetAdvancedFilterFunnel() {
    this.resetAdvancedFilterFunnelFormEvent$.next();
  }

  resetJobFilterFunnel() {
    this.resetJobFilterFunnelFormEvent$.next();
  }

  setFilterHistoryFormValue(filter: FilterHistory) {
    this.filterHistoryFormEvent$.next(filter);
  }

  setDrawerOpenState(isOpen: boolean) {
    this.drawerOpen$.next(isOpen);
  }
}

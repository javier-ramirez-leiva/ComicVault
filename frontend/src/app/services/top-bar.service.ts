import { Injectable, inject } from '@angular/core';
import { ReplaySubject, Subject } from 'rxjs';
import { Category } from 'interfaces';
import { FilterFunnel } from 'components';
import { FilterHistory } from 'interfaces';
import { Router } from '@angular/router';
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

  filterEvent$: ReplaySubject<FilterFunnel> = new ReplaySubject<FilterFunnel>();
  advanceFilterEvent$: ReplaySubject<AdvanceFilterFunnel> =
    new ReplaySubject<AdvanceFilterFunnel>();
  contentChange$: ReplaySubject<boolean> = new ReplaySubject<boolean>();
  searchBarTextModifierLib$: Subject<string> = new Subject<string>();
  searchBarTextModifierSearch$: Subject<string> = new Subject<string>();

  private readonly activePageService = inject(ActivePageService);

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

  setFilterHistoryFormValue(filter: FilterHistory) {
    this.filterHistoryFormEvent$.next(filter);
  }
}

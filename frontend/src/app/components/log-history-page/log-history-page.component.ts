import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, Observable, switchMap, tap } from 'rxjs';
import { Log, LogFilter } from 'interfaces';
import { PageNavigatorComponent } from '../page-navigator/page-navigator.component';
import { CommonModule } from '@angular/common';
import { LogsService, TopBarService } from 'services';
import { LogHistoryTableComponent } from '../log-history-table/log-history-table.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 'app-log-history-page',
  imports: [PageNavigatorComponent, CommonModule, LogHistoryTableComponent],
  templateUrl: './log-history-page.component.html',
})
export class LogHistoryPageComponent implements OnInit {
  private readonly logService = inject(LogsService);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly router: Router = inject(Router);
  private readonly topBarService = inject(TopBarService);

  filter: LogFilter | undefined;

  logs$: Observable<Log[]>;
  page: number = 1;

  constructor() {
    this.logs$ = this.route.queryParams.pipe(
      filter((params) => !!params['page'] || !!params['filter']),
      switchMap((params) => {
        this.page = params['page'] ? params['page'] : 1;
        this.filter = params['filter'] ? JSON.parse(params['filter']) : undefined;
        return this.logService.getHistoryLogs(this.page, this.filter);
      }),
    );
  }

  ngOnInit() {
    this.topBarService.logFilterEvent$.pipe(untilDestroyed(this)).subscribe((filter) => {
      this.router.navigate(['settings', 'logs'], {
        queryParams: { filter: JSON.stringify(filter) },
      });
    });
  }

  onPageChange(page: number) {
    this.page = page;
    this.navigate();
  }

  private navigate() {
    this.router.navigate(['/settings/logs'], {
      queryParams: { page: this.page, filter: JSON.stringify(this.filter) },
    });
  }
}

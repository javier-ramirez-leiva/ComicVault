import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, switchMap, tap } from 'rxjs';
import { Log } from 'interfaces';
import { PageNavigatorComponent } from '../page-navigator/page-navigator.component';
import { CommonModule } from '@angular/common';
import { LogsService } from 'services';
import { LogHistoryTableComponent } from '../log-history-table/log-history-table.component';

@Component({
    selector: 'app-log-history-page',
    imports: [PageNavigatorComponent, CommonModule, LogHistoryTableComponent],
    templateUrl: './log-history-page.component.html'
})
export class LogHistoryPageComponent {
    private readonly logService = inject(LogsService);
    private readonly route: ActivatedRoute = inject(ActivatedRoute);
    private readonly router: Router = inject(Router);

    logs$: Observable<Log[]>;
    page: number = 1;

    constructor() {
        this.logs$ = this.route.queryParams.pipe(
            tap(d => console.log(d)),
            switchMap((params) => {
                this.page = params['page'] ? params['page'] : 1;
                return this.logService.getHistoryLogs(this.page);
            })
        );
    }

    onPageChange(page: number) {
        this.page = page;
        this.navigate();
    }

    private navigate() {
        this.router.navigate(['/settings/logs'], { queryParams: { page: this.page } });
    }
}

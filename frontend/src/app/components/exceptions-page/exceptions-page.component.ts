import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, Observable, startWith, Subject, switchMap } from 'rxjs';
import { Exception } from 'src/app/interfaces/exception';
import { ExceptionService } from 'src/app/services/exception.service';
import { PageNavigatorComponent } from '../page-navigator/page-navigator.component';
import { ExceptionsTableComponent } from '../exceptions-table/exceptions-table.component';
import { CommonModule } from '@angular/common';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { S } from 'node_modules/@angular/cdk/scrolling-module.d-ud2XrbF8';

@UntilDestroy()
@Component({
  selector: 'app-exceptions-page',
  imports: [PageNavigatorComponent, ExceptionsTableComponent, CommonModule],
  templateUrl: './exceptions-page.component.html',
})
export class ExceptionsPageComponent {
  private readonly exceptionService = inject(ExceptionService);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly router: Router = inject(Router);

  exceptions$: Observable<Exception[]>;
  private readonly triggerReload$ = new Subject<void>();
  page: number = 1;

  constructor() {
    this.exceptions$ = combineLatest([
      this.route.queryParams,
      this.triggerReload$.pipe(startWith(null)),
    ]).pipe(
      switchMap(([params, _]) => {
        this.page = params['page'] ? params['page'] : 1;
        return this.exceptionService.getExceptions(this.page);
      }),
    );
  }

  onPageChange(page: number) {
    this.page = page;
    this.navigate();
  }

  deleteAllExceptions() {
    this.exceptionService
      .deleteAll()
      .pipe(untilDestroyed(this))
      .subscribe(() => {
        this.triggerReload$.next();
      });
  }

  private navigate() {
    this.router.navigate(['/settings/exceptions'], { queryParams: { page: this.page } });
  }
}

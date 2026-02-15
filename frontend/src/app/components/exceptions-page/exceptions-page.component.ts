import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, switchMap } from 'rxjs';
import { Exception } from 'src/app/interfaces/exception';
import { ExceptionService } from 'src/app/services/exception.service';
import { PageNavigatorComponent } from '../page-navigator/page-navigator.component';
import { ExceptionsTableComponent } from '../exceptions-table/exceptions-table.component';
import { CommonModule } from '@angular/common';

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
  page: number = 1;

  constructor() {
    this.exceptions$ = this.route.queryParams.pipe(
      switchMap((params) => {
        this.page = params['page'] ? params['page'] : 1;
        return this.exceptionService.getExceptions(this.page);
      }),
    );
  }

  onPageChange(page: number) {
    this.page = page;
    this.navigate();
  }

  private navigate() {
    this.router.navigate(['/settings/jobs'], { queryParams: { page: this.page } });
  }
}

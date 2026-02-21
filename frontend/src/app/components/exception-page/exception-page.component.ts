import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { map, Observable, switchMap } from 'rxjs';
import { Exception } from 'src/app/interfaces/exception';
import { ExceptionService } from 'src/app/services/exception.service';
import { ExceptionsTableComponent } from '../exceptions-table/exceptions-table.component';

@Component({
  selector: 'app-exception-page',
  imports: [CommonModule, ExceptionsTableComponent],
  templateUrl: './exception-page.component.html',
})
export class ExceptionPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly exceptionService = inject(ExceptionService);

  protected readonly exception$: Observable<Exception>;

  constructor() {
    const id$: Observable<string> = this.route.params.pipe(map((params) => params['id']));

    this.exception$ = id$.pipe(
      switchMap((exceptionId) => this.exceptionService.getException(Number(exceptionId))),
    );
  }
}

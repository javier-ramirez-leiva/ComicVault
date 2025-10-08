import { Component, Input, OnInit, inject } from '@angular/core';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { EMPTY, Observable, catchError, of } from 'rxjs';
import { ConfigService, NotifierService } from 'services';
import { CommonModule } from '@angular/common';
import { HttpResponseError } from 'interfaces';

@Component({
  selector: 'app-test-button',
  imports: [CommonModule],
  templateUrl: './test-button.component.html'
})

@UntilDestroy()
export class TestButtonComponent implements OnInit {

  @Input({ required: true }) test!: 'storage' | 'slack' | 'comicvine' | 'database' | 'jdownloader' | 'getcomicsBaseUrl';
  @Input({ required: true }) payload!: any;
  @Input({ required: false }) resetStatus$: Observable<void> | undefined;
  testResult: boolean | null = null;
  private apiCall$: Observable<any> = new Observable<any>();

  private readonly configService = inject(ConfigService);
  private readonly notifier = inject(NotifierService);

  ngOnInit(): void {
    if (this.resetStatus$) {
      this.resetStatus$.pipe(untilDestroyed(this)).subscribe(() => {
        this.testResult = null;
      });
    }
  }


  getAPIObservable(): Observable<any> {
    switch (this.test) {
      case 'storage':
        return this.configService.setStoragePath(this.payload);
      case 'slack':
        return this.configService.setSlackConfiguration(this.payload);
      case 'comicvine':
        return this.configService.setComicVineAPIKey(this.payload);
      case 'database':
        return this.configService.setDBExpression(this.payload);
      case 'jdownloader':
        return this.configService.setJDownloaderConfiguration(this.payload);
      case 'getcomicsBaseUrl':
        return this.configService.setgetcomicsBaseUrl(this.payload);
    }
  }

  testAPI() {
    this.getAPIObservable().pipe(
      catchError((errorResponse) => {
        this.testResult = false;
        const error: HttpResponseError = errorResponse.error;
        let errorMessage = 'An unexpected error occurred.';
        errorMessage = error.message;
        this.notifier.appendNotification({
          id: 0,
          title: 'Error',
          message: error.message,
          type: 'error',
        });
        return EMPTY;
      }),
      untilDestroyed(this)
    ).subscribe((response) => {
      this.testResult = true;
    });
  }
}
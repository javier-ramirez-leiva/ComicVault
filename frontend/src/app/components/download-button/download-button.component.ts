import { Component, Input, inject } from '@angular/core';
import { ComicsSearch, ComicSearchDetailsLinks, isHttpResponseError, } from 'interfaces';
import { ComicsService, ModalService, NotifierService } from 'services';
import { catchError, delay, switchMap, tap } from 'rxjs/operators';
import { Subject, of } from 'rxjs';
import { CommonModule } from '@angular/common';
import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';


@Component({
  selector: 'app-download-button',
  imports: [CommonModule],
  templateUrl: './download-button.component.html'
})
export class DownloadButtonComponent {

  private readonly comicsService: ComicsService = inject(ComicsService);
  private readonly triggerDialog$ = new Subject<void>();
  private readonly modalService = inject(ModalService);
  private readonly notifierService = inject(NotifierService);

  @Input({ required: true }) comic!: ComicsSearch;
  @Input({ required: false }) comicSearchDetailsLinks: ComicSearchDetailsLinks | undefined;
  @Input({ required: false }) mini: boolean = false;

  constructor() {
    this.triggerDialog$.pipe(
      tap(() => this.modalService.loadingAnimation(true)),
      switchMap(() => {
        if (this.comicSearchDetailsLinks) {
          return of(this.comicSearchDetailsLinks);
        } else {
          return this.comicsService.getComicSearchDetailsLinks(this.comic.idGc)
        }
      }),
      catchError(catchError => {
        const error = catchError.error;
        if (isHttpResponseError(error) && (error.errorCode === 'SCRAPER_GATEWAY_ERROR' || error.errorCode === 'SCRAPER_PARSING_ERROR')) {
          this.notifierService.appendNotification({
            id: 0,
            title: 'Error',
            message: catchError.error.message as string,
            type: 'error'
          });
        }
        return of(undefined);
      }),
    ).subscribe(comicSearchDetailsLinks => {
      this.modalService.loadingAnimation(false);
      if (comicSearchDetailsLinks) {
        resetRouteCache();
        this.comicsService.downloadWithNotification(this.comic, comicSearchDetailsLinks);
      }
    });
  }


  downloadDialog(event: Event) {
    event.stopPropagation();
    this.triggerDialog$.next();
  }

}
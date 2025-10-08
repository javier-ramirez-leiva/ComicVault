import { Component, Input, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ComicSearchDetailsLinks, isHttpResponseError } from 'interfaces';
import { EMPTY, catchError } from 'rxjs';
import { ComicsService, NotifierService } from 'services';

@UntilDestroy()
@Component({
  selector: 'app-go-to-comic-button',
  imports: [],
  templateUrl: './go-to-comic-button.component.html'
})
export class GoToComicButtonComponent {
  @Input({ required: true }) comicSearchDetailsLinks!: ComicSearchDetailsLinks;

  private readonly router = inject(Router);
  private readonly comicService = inject(ComicsService);
  private readonly notifierService = inject(NotifierService);


  goToComic(event: Event) {
    event.stopPropagation();
    this.comicService.getComicByidGc(this.comicSearchDetailsLinks.idGc).pipe(
      catchError((catchError) => {
        const error = catchError.error;
        if (isHttpResponseError(error)) {
          this.notifierService.appendNotification({
            message: 'There was an error when adding comic to library',
            id: 0,
            title: 'Error!',
            type: 'error'
          })
        }
        return EMPTY;
      }),
      untilDestroyed(this)
    )
      .subscribe(comic => {
        this.router.navigate(['/comics', comic.id, 'details']);
      });
  }
}

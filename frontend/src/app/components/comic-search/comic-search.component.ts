import { Component, Input, inject } from '@angular/core';
import { ComicsSearch } from 'interfaces';
import { ComicsService } from 'services';
import { CoverCardComponent } from '../cover-card/cover-card.component';
import { UntilDestroy } from '@ngneat/until-destroy';
import { Observable, concatMap, filter, map, of, shareReplay, switchMap, takeUntil, tap } from 'rxjs';
import { CommonModule } from '@angular/common';
import { distinctUntilValueChanged } from 'src/app/utils/rsjx-operators';


@UntilDestroy()
@Component({
  selector: 'app-comic-search',
  imports: [CoverCardComponent, CommonModule],
  templateUrl: './comic-search.component.html'
})
export class ComicSearchComponent {
  @Input({ required: true }) public comic!: ComicsSearch;
  @Input({ required: false }) public queryParams: any = null;
  comicsService: ComicsService = inject(ComicsService);

  private readonly comicUpdated$: Observable<ComicsSearch>;
  private comicUpdated: ComicsSearch | undefined = undefined;
  protected readonly progress$: Observable<number>;
  protected readonly remainingComic$: Observable<number | null>;
  protected readonly template$: Observable<Template>;

  private downloaded: boolean = false;


  constructor() {
    this.comicUpdated$ = this.comicsService.downloadingList$.pipe(
      filter(() => !this.downloaded),
      concatMap(comics => {
        const downloadingComic = comics.find(comic => comic.idGc === this.comic.idGc);
        if (downloadingComic) {
          downloadingComic.downloadingStatus = "downloading";
          return of(downloadingComic);
          //If the previous query comic was downloading, call to get the updated comic
        } else if (this.comicUpdated?.downloadingStatus === "downloading") {
          return this.comicsService.getComicSearchByidGc(this.comic.idGc);
        } else {
          return of(this.comic)
        }
      }),
      tap(comicUpdated => this.comicUpdated = comicUpdated),
      distinctUntilValueChanged(),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    this.progress$ = this.comicUpdated$.pipe(
      map(comic => {
        if (comic.currentBytes > 0 && comic.downloadingStatus !== "downloaded") {
          return 100 * (comic.currentBytes / comic.totalBytes)
        } else {
          return 0;
        }
      })
    );

    this.remainingComic$ = this.comicUpdated$.pipe(
      filter(comic => comic.totalComics > 1),
      map(comic => (comic.currentComic === (comic.totalComics - 1) && comic.totalBytes === comic.currentBytes) ? null : comic.totalComics - comic.currentComic)
    );

    this.template$ = this.comicUpdated$.pipe(
      switchMap(comic => {
        if (comic.downloadingStatus === "downloaded") {
          this.downloaded = true;
          return this.comicsService.getComicByidGc(comic.idGc).pipe(
            map(comicDatabase => ({
              url: ['/comics', comicDatabase.id, "details"],
              checked: true,
              displayButton: false
            }))
          );
        } else {
          return of({
            url: ['/comics-search', comic.idGc, "details"],
            checked: false,
            displayButton: comic.currentBytes <= 0
          })
        };
      })
    );
  }
}

type Template = {
  url: string[];
  checked: boolean;
  displayButton: boolean;
}

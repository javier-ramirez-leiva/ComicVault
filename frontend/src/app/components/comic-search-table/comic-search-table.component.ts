import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnInit } from '@angular/core';
import { Category, ComicsDatabase, ComicsSearch, DownloadStatus } from 'interfaces';
import { PublisherCellComponent } from '../publisher-cell/publisher-cell.component';
import { RouterLink } from '@angular/router';
import { EMPTY, filter, map, Observable, of, switchMap, tap } from 'rxjs';
import { ComicsService, ConfigURLService } from 'services';
import { distinctUntilValueChanged, notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { DownloadButtonComponent } from "../download-button/download-button.component";

@Component({
  selector: 'app-comic-search-table',
  imports: [CommonModule, RouterLink, PublisherCellComponent, DownloadButtonComponent],
  templateUrl: './comic-search-table.component.html',
})
export class ComicSearchTableComponent {
  private _comics!: ComicsSearch[];

  @Input({ required: true })
  set comics(value: ComicsSearch[]) {
    this._comics = value;
    this.comicObservables = this._comics.map(comic => this.buildComicUpdated(comic));
  }

  get comics(): ComicsSearch[] {
    return this._comics;
  }

  @Input({ required: false }) displayAddButton = true;

  private readonly comicsService = inject(ComicsService);
  private readonly configURLService = inject(ConfigURLService);

  protected comicObservables: Observable<ComicSearchRow>[] = [];

  lastDownloadComics: ComicsSearch[] = [];
  private cachedComicsDB: Map<string, ComicSearchRow> = new Map();

  buildComicUpdated(comic: ComicsSearch): Observable<ComicSearchRow> {
    return this.comicsService.downloadingList$.pipe(
      switchMap(downloadingList => {
        const downloadingComic = downloadingList.find(downloadingComic => downloadingComic.idGc === comic.idGc);
        if (downloadingComic) {
          const progress = 100 * (downloadingComic.currentBytes / downloadingComic.totalBytes);
          this.lastDownloadComics.push(...downloadingList);
          return of({
            title: comic.title,
            url: `/comics-search/${comic.idGc}/details`,
            image: comic.image,
            category: comic.category,
            downloadingStatus: 'downloading' as DownloadStatus,
            year: comic.year,
            size: comic.size,
            progress: `width: ${progress}%`,
            original: comic
          });
        }
        if (comic.downloadingStatus === 'downloaded' || this.lastDownloadComics.find(downloadingComic => downloadingComic.idGc === comic.idGc)) {
          const cachedComic = this.cachedComicsDB.get(comic.idGc);
          if (cachedComic) {
            return of(cachedComic)
          }
          return this.comicsService.getComicByidGc(comic.idGc).pipe(
            filter(notNullOrUndefined()),
            map(comicDB => ({
              title: comicDB.title,
              url: `/comics/${comicDB.id}/details`,
              image: this.configURLService.baseURL + '/' + this.configURLService.apiVersion + '/comics/' + comicDB.id + '/cover/small',
              category: comicDB.category,
              downloadingStatus: 'downloaded' as DownloadStatus,
              year: comicDB.year,
              size: comicDB.size,
              progress: undefined,
              original: comic
            })),
            tap(comicRow => this.cachedComicsDB.set(comic.idGc, comicRow)),
          )
        }
        if (comic.downloadingStatus === 'not-downloaded' && !downloadingComic) {
          return of({
            title: comic.title,
            url: `/comics-search/${comic.idGc}/details`,
            image: comic.image,
            category: comic.category,
            downloadingStatus: comic.downloadingStatus,
            year: comic.year,
            size: comic.size,
            progress: undefined,
            original: comic
          });
        }
        //SHOULD NEVER HAPPEN
        return EMPTY;
      }),
      distinctUntilValueChanged(),
    );
  }
}

export type ComicSearchRow = {
  title: string;
  url: string;
  image: string;
  category: Category;
  downloadingStatus: DownloadStatus;
  year: string;
  size: string;
  progress: string | undefined;
  original: ComicsSearch
}
import { HttpClient, HttpEventType, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ComicsDatabase } from 'interfaces';
import { ConfigURLService } from './configURL.service';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { BehaviorSubject, Observable, Subject, tap } from 'rxjs';
import { AuthService } from './auth.service';
import { ComicDatabaseComponent } from 'components';

@Injectable({
  providedIn: 'root',
})
@UntilDestroy()
export class DownloadService {
  private readonly authService = inject(AuthService);
  private readonly httpClient = inject(HttpClient);
  private readonly configURLService = inject(ConfigURLService);
  private readonly comicLocalDownload$ = new BehaviorSubject<ComicsDatabaseWithDownload[]>([]);

  getComicPage(id: string, page: number): Observable<Blob> {
    return this.authService.handleApiCallError(() => {
      return this.httpClient.get<Blob>(
        `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${id}/pages/${page}`,
        { responseType: 'blob' as 'json' },
      );
    });
  }

  downloadComic(comic: ComicsDatabase): void {
    this.authService
      .handleApiCallError(() => {
        return this.httpClient.get(
          `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${comic.id}/download`,
          { observe: 'response', responseType: 'blob' },
        );
      })
      .pipe(untilDestroyed(this))
      .subscribe((response) => {
        const contentDisposition = response.headers.get('content-Disposition');
        const filenameFromPath = this.getFileNameFromPath(comic.path);
        const filename = contentDisposition
          ? this.getFilenameFromContentDisposition(contentDisposition)
          : 'comic.cbr';
        if (response.body) {
          const blobUrl = window.URL.createObjectURL(response.body);
          const link = document.createElement('a');
          link.href = blobUrl;
          link.download = filenameFromPath;
          link.click();
          window.URL.revokeObjectURL(blobUrl);
        }
      });
  }

  downloadComicProgress(comic: ComicsDatabase): void {
    const progress$ = new Subject<number>(); // Subject to track progress

    this.authService
      .handleApiCallError(() =>
        this.httpClient.get(
          `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${comic.id}/download`,
          {
            observe: 'events',
            responseType: 'blob',
            reportProgress: true,
          },
        ),
      )
      .pipe(untilDestroyed(this))
      .subscribe({
        next: (event) => {
          if (event.type === HttpEventType.DownloadProgress) {
            const progress = event.total ? Math.round((event.loaded / event.total) * 100) : 0;
            progress$.next(progress); // Emit progress percentage
          } else if (event.type === HttpEventType.Response) {
            const response = event as HttpResponse<Blob>;
            const contentDisposition = response.headers.get('content-disposition');
            const filenameFromPath = this.getFileNameFromPath(comic.path);
            const filename = contentDisposition
              ? this.getFilenameFromContentDisposition(contentDisposition)
              : 'comic.cbr';

            if (response.body) {
              const blobUrl = window.URL.createObjectURL(response.body);
              const link = document.createElement('a');
              link.href = blobUrl;
              link.download = filenameFromPath;
              link.click();
              window.URL.revokeObjectURL(blobUrl);

              // Remove from download list
              const comicLocalDownload = this.comicLocalDownload$.value.filter(
                (item) => item.comic.id !== comic.id,
              );
              this.comicLocalDownload$.next(comicLocalDownload);
            }

            progress$.complete(); // Complete progress tracking
          }
        },
        error: (err) => progress$.error(err),
      });

    // Add comic to the local download list with its progress observable
    this.comicLocalDownload$.next([
      ...this.comicLocalDownload$.value,
      { comic, progress$: progress$.asObservable() },
    ]);
  }

  downloadComicList(comics: ComicsDatabase[]): void {
    const progress$ = new Subject<number>(); // Subject to track progress

    const comicIDs = comics.map((comic) => comic.id);

    this.authService
      .handleApiCallError(() =>
        this.httpClient.post(
          `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comicList/download`,
          comicIDs,
          {
            observe: 'events',
            responseType: 'blob',
            reportProgress: true,
          },
        ),
      )
      .pipe(untilDestroyed(this))
      .subscribe({
        next: (event) => {
          if (event.type === HttpEventType.DownloadProgress) {
            const progress = event.total ? Math.round((event.loaded / event.total) * 100) : 0;
            progress$.next(progress); // Emit progress percentage
          } else if (event.type === HttpEventType.Response) {
            const response = event as HttpResponse<Blob>;
            const contentDisposition = response.headers.get('content-disposition');
            const filename = contentDisposition
              ? this.getFilenameFromContentDisposition(contentDisposition)
              : 'comic.zip';

            if (response.body) {
              const blobUrl = window.URL.createObjectURL(response.body);
              const link = document.createElement('a');
              link.href = blobUrl;
              link.download = filename;
              link.click();
              window.URL.revokeObjectURL(blobUrl);

              // Remove downloaded comics from the list
              const remainingComics = this.comicLocalDownload$.value.filter(
                (item) => !comicIDs.includes(item.comic.id),
              );
              this.comicLocalDownload$.next(remainingComics);
            }

            progress$.complete(); // Complete the observable when done
          }
        },
        error: (err) => progress$.error(err),
      });

    const newDownloads = comics.map((comic) => ({ comic, progress$: progress$.asObservable() }));
    this.comicLocalDownload$.next([...this.comicLocalDownload$.value, ...newDownloads]);
    progress$.pipe(untilDestroyed(this)).subscribe();
  }

  getLocalDownload(): Observable<ComicsDatabaseWithDownload[]> {
    return this.comicLocalDownload$.asObservable();
  }

  private getFilenameFromContentDisposition(contentDisposition: string): string {
    const filenameMatch = contentDisposition.match(/filename="?(.+?)"?$/);
    return filenameMatch ? filenameMatch[1] : 'downloaded-file';
  }

  private getFileNameFromPath(path: string): string {
    let pathParts = path.split('/');
    if (pathParts.length === 0) {
      pathParts = path.split('\\');
    }
    const fileName = pathParts[pathParts.length - 1];
    return fileName;
  }
}

export type ComicsDatabaseWithDownload = {
  comic: ComicsDatabase;
  progress$: Observable<number>;
};

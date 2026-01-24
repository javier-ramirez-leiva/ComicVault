import { Injectable, inject, isDevMode } from '@angular/core';
import {
  BehaviorSubject,
  EMPTY,
  Observable,
  ReplaySubject,
  catchError,
  distinctUntilChanged,
  filter,
  interval,
  map,
  startWith,
  switchMap,
} from 'rxjs';
import {
  ComicsDatabase,
  ComicsSearch,
  Series,
  DownloadLink,
  DownloadRequest,
  ComicSearchDetailsLinks,
  DownloadRequestList,
  ScrapperResponse,
  DownloadIssueRequest,
  DownloadIssue,
  DeleteReadOptions,
} from 'interfaces';
import { HttpService } from './http.service';
import { ModalService } from './modal.service';
import { ModalDownloadComponent } from '../components/modal-download/modal-download.component';
import { NotifierService } from './notifier.service';
import { ModalDownloadListComponent } from '../components/modal-download-list/modal-download-list.component';
import { ActivePageService } from './active-page.service';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@Injectable({
  providedIn: 'root',
})
@UntilDestroy()
export class ComicsService {
  private readonly updateNotification: BehaviorSubject<null> = new BehaviorSubject<null>(null);
  readonly downloadingList$: ReplaySubject<ComicsSearch[]> = new ReplaySubject<ComicsSearch[]>(1);

  private readonly httpService = inject(HttpService);
  private readonly modalService = inject(ModalService);
  private readonly notifierService = inject(NotifierService);
  private readonly activePageService = inject(ActivePageService);

  constructor() {
    this.activePageService.activePage$
      .pipe(
        distinctUntilChanged(),
        switchMap((page) => {
          if (page === 'search' || page === 'downloads') {
            return interval(500).pipe(
              startWith(0),
              switchMap(() => this.downloads()),
            );
          } else {
            // Return empty observable to stop polling
            return EMPTY;
          }
        }),
        untilDestroyed(this),
      )
      .subscribe((comics) => this.downloadingList$.next(comics));
  }

  allComics(): Observable<ComicsDatabase[]> {
    return this.httpService.request<ComicsDatabase[]>('GET', `/comics`);
  }

  ongoingComics(): Observable<ComicsDatabase[]> {
    return this.httpService.request<ComicsDatabase[]>('GET', `/comics/ongoing`);
  }

  newComics(): Observable<ComicsDatabase[]> {
    return this.httpService.request<ComicsDatabase[]>('GET', `/comics/new`);
  }

  allSeries(): Observable<Series[]> {
    return this.httpService.request<Series[]>('GET', `/series`);
  }

  ongoingSeries(): Observable<Series[]> {
    return this.httpService.request<Series[]>('GET', `/series/ongoing`);
  }

  newSeries(): Observable<Series[]> {
    return this.httpService.request<Series[]>('GET', `/series/new`);
  }

  trending(category: string, page: number, pageRatio: number): Observable<ScrapperResponse> {
    const endpoint =
      category === 'all'
        ? `/latest?page=${page}&pageRatio=${pageRatio}`
        : `/category?page=${page}&category=${category}&pageRatio=${pageRatio}`;

    return this.httpService.request<ScrapperResponse>('GET', endpoint);
  }

  search(query: string, page: number, pageRatio: number): Observable<ScrapperResponse> {
    return this.httpService.request<ScrapperResponse>(
      'GET',
      `/search?query=${query}&page=${page}&pageRatio=${pageRatio}`,
    );
  }

  tag(tag: string, page: number, pageRatio: number): Observable<ScrapperResponse> {
    return this.httpService.request<ScrapperResponse>(
      'GET',
      `/tag?tag=${tag}&page=${page}&pageRatio=${pageRatio}`,
    );
  }

  scanLib(): Observable<boolean> {
    return this.httpService.request<boolean>('POST', `/scanLib`);
  }

  getComicSearchDetailsLinks(idGc: string): Observable<ComicSearchDetailsLinks> {
    return this.httpService.request<ComicSearchDetailsLinks>('GET', `/searchs/${idGc}/details`);
  }

  download(downloadRequest: DownloadRequest): Observable<string> {
    return this.httpService.request<string>('POST', `/download`, downloadRequest);
  }

  downloadList(downloadRequest: DownloadRequestList): Observable<string> {
    return this.httpService.request<string>('POST', `/downloadList`, downloadRequest);
  }

  downloadWithNotification(
    comic: ComicsSearch,
    comicSearchDetailsLinks: ComicSearchDetailsLinks,
  ): void {
    let observable$ = undefined;
    if (comicSearchDetailsLinks.downloadIssues.length === 1) {
      observable$ = this.modalService
        .open<
          DownloadLink,
          { options: DownloadLink[] | undefined; comicTitle: string }
        >(ModalDownloadComponent, { options: comicSearchDetailsLinks.downloadIssues[0].links, comicTitle: comicSearchDetailsLinks.title })
        .pipe(
          filter((response) => response !== undefined),
          map((response) => [response]),
          switchMap((response) => {
            this.notifierService.appendProcessingNotification({
              id: 0,
              message: `${comic.title} downloading`,
              type: 'download',
            });
            const downloadRequest: DownloadRequest = {
              comicSearchDetails: comicSearchDetailsLinks ?? {
                ...comic,
                description: '',
                tags: [],
                mainTag: null,
              },
              downloadLink: response[0],
            };
            return this.download(downloadRequest).pipe(
              catchError((error) => {
                if (isDevMode()) {
                  this.notifierService.appendNotification({
                    id: 0,
                    title: 'Error',
                    message: error.message,
                    type: 'error',
                  });
                }
                return EMPTY;
              }),
            );
          }),
        );
    }
    if (comicSearchDetailsLinks.downloadIssues.length > 1) {
      observable$ = this.modalService
        .open<
          DownloadIssueRequest[],
          { options: DownloadIssue[] | undefined; comicTitle: string }
        >(ModalDownloadListComponent, { options: comicSearchDetailsLinks.downloadIssues, comicTitle: comicSearchDetailsLinks.title })
        .pipe(
          filter((response) => response !== undefined),
          switchMap((response) => {
            this.notifierService.appendProcessingNotification({
              id: 0,
              message: `${comic.title} downloading ${comicSearchDetailsLinks.downloadIssues.length} issues`,
              type: 'download',
            });
            const downloadRequestList: DownloadRequestList = {
              comicSearchDetails: comicSearchDetailsLinks ?? {
                ...comic,
                description: '',
                tags: [],
                mainTag: null,
              },
              downloadRequests: response.filter(
                (downloadIssueRequest) => downloadIssueRequest.link !== undefined,
              ),
            };
            return this.downloadList(downloadRequestList).pipe(
              catchError((error) => {
                if (isDevMode()) {
                  this.notifierService.appendNotification({
                    id: 0,
                    title: 'Error',
                    message: error.message,
                    type: 'error',
                  });
                }
                return EMPTY;
              }),
            );
          }),
        );
    }
    if (observable$ !== undefined) {
      observable$.subscribe(() => {
        this.notifierService.appendNotification({
          id: 0,
          title: 'Download finished',
          message: comic.title,
          type: 'info',
        });
        this.sendUpdatedNotification();
      });
    }
    return;
  }

  downloads(): Observable<ComicsSearch[]> {
    return this.httpService.request<ComicsSearch[]>('GET', `/downloads`);
  }

  getComic(id: string): Observable<ComicsDatabase> {
    return this.httpService.request<ComicsDatabase>('GET', `/comics/search?id=${id}`);
  }

  getComicByidGc(idGc: string): Observable<ComicsDatabase> {
    return this.httpService.request<ComicsDatabase>('GET', `/comics/search?idGc=${idGc}`);
  }

  getComicSearchByidGc(idGc: string): Observable<ComicsSearch> {
    return this.httpService.request<ComicsSearch>('GET', `/searchs/search?idGc=${idGc}`);
  }

  getSeriesByID(id: string): Observable<Series> {
    return this.httpService.request<Series>('GET', `/series/search?id=${id}`);
  }

  setComicProperties(id: string, properties: any): Observable<any> {
    return this.httpService.request<any>('POST', `/comics/${id}`, properties);
  }

  deleteComic(id: string): Observable<any> {
    return this.httpService.request<any>('DELETE', `/comics/${id}`);
  }

  cleanLibrary(): Observable<boolean> {
    return this.httpService.request<boolean>('DELETE', `/comics/clean`);
  }

  deleteReadComics(deleteReadOption: DeleteReadOptions): Observable<boolean> {
    return this.httpService.request<boolean>('POST', `/comics/deleteRead`, deleteReadOption);
  }

  sendUpdatedNotification(): void {
    this.updateNotification.next(null);
  }

  getUpdateNotification(): Observable<null> {
    return this.updateNotification.asObservable();
  }

  setComicPageStatus(id: string, page: number) {
    return this.httpService.request<any>('POST', `/comics/${id}/pageStatus`, page);
  }

  setComicListReadStatus(IDs: string[], value: boolean): Observable<any> {
    const endpoint = value ? 'markAsRead' : 'markAsNotRead';
    return this.httpService.request<any>('POST', `/comicList/${endpoint}`, IDs);
  }

  deleteComicList(IDs: string[]): Observable<any> {
    return this.httpService.request<any>('POST', `/comicList/delete`, IDs);
  }

  setCover(id: string, page: number) {
    return this.httpService.request<any>('POST', `/comics/${id}/setCover`, page);
  }

  setIssues(comicIdsIssues: Map<string, number>) {
    const payload = Object.fromEntries(comicIdsIssues);
    return this.httpService.request<any>('POST', `/comicList/setIssues`, payload);
  }

  setSeriesProperties(id: string, properties: any): Observable<any> {
    return this.httpService.request<any>('POST', `/series/${id}`, properties);
  }
}

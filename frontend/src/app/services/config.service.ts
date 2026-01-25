import { Injectable, inject } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs';
import { ComicsConfiguration, JDownloaderConfiguration, SlackConfiguration } from 'interfaces';

@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  private readonly httpService = inject(HttpService);

  getConfig(): Observable<ComicsConfiguration> {
    return this.httpService.request<ComicsConfiguration>('GET', `/configuration`);
  }

  setStoragePath(path: string): Observable<any> {
    return this.httpService.request<any>('POST', `/configuration/downloadRoot`, path);
  }

  setSlackConfiguration(slackConfiguration: SlackConfiguration): Observable<any> {
    return this.httpService.request<any>('POST', `/configuration/slack`, slackConfiguration);
  }

  setgetcomicsBaseUrl(getComicsBaseUrl: string): Observable<any> {
    return this.httpService.request<any>(
      'POST',
      `/configuration/getComicsBaseUrl`,
      getComicsBaseUrl,
    );
  }

  setScanArchives(value: boolean): Observable<any> {
    return this.httpService.request<any>('POST', `/configuration/scanArchives`, value);
  }

  setDeleteArchives(value: boolean): Observable<any> {
    return this.httpService.request<any>('POST', `/configuration/deleteArchives`, value);
  }

  setGenerateNavigationThumbnails(value: boolean): Observable<any> {
    return this.httpService.request<any>(
      'POST',
      `/configuration/generateNavigationThumbnails`,
      value,
    );
  }

  setEnableSlackNotifications(value: boolean): Observable<any> {
    return this.httpService.request<any>('POST', `/configuration/slackNotify`, value);
  }

  setComicVineAPIKey(key: string): Observable<any> {
    return this.httpService.request<any>('POST', `/configuration/comicVine_apiKey`, key);
  }

  setDBExpression(expression: string): Observable<any> {
    return this.httpService.request<any>('POST', `/configuration/dbExpression`, expression);
  }

  setJDownloaderConfiguration(configuration: JDownloaderConfiguration): Observable<any> {
    return this.httpService.request<any>('POST', `/configuration/jDownloader`, configuration);
  }
}

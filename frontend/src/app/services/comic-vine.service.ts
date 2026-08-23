import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ComicVineIssueInfo } from '../interfaces/comicVine';
import { HttpService } from './http.service';

@Injectable({
  providedIn: 'root',
})
export class ComicVineService {
  private readonly httpService = inject(HttpService);

  searchIssue(issue: string): Observable<ComicVineIssueInfo | null> {
    return this.httpService.request<ComicVineIssueInfo | null>(
      'GET',
      `/comicVine/search?issue=${encodeURIComponent(issue)}`,
    );
  }
}

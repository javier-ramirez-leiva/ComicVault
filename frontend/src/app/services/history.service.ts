import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { History } from 'interfaces';
import { HttpService } from './http.service';
import { FilterHistory } from 'interfaces';

@Injectable({
  providedIn: 'root'
})
export class HistoryService {

  private readonly httpService = inject(HttpService);

  historyMe(filterHistory: FilterHistory): Observable<History[]> {
    return this.httpService.request<History[]>(
      'POST',
      `/history/me`,
      filterHistory
    );
  }


  historyUsername(username: string, filterHistory: FilterHistory): Observable<History[]> {
    return this.httpService.request<History[]>(
      'POST',
      `/history/user?username=${username}`,
      filterHistory
    );
  }

}

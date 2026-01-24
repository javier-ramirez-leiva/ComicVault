import { inject, Injectable } from '@angular/core';
import { ServerInfo } from 'interfaces';
import { HttpService } from './http.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ServerInfoService {
  private readonly httpService = inject(HttpService);

  getServerInfo(): Observable<ServerInfo> {
    return this.httpService.request<ServerInfo>('GET', `/serverInfo`);
  }
}

import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { ConfigURLService } from './configURL.service';

@Injectable({
  providedIn: 'root',
})
export class HttpService {
  private readonly httpClient = inject(HttpClient);
  private readonly configURLService = inject(ConfigURLService);
  private readonly authService = inject(AuthService);

  constructor() {}

  request<T>(verb: HTTPVerb, endpoint: string, data?: any): Observable<T> {
    return this.authService.handleApiCallError(() => {
      if (!this.configURLService.baseURL) {
        return throwError(() => new Error('Base URL is not defined'));
      }

      const url = `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/${endpoint}`;

      switch (verb) {
        case 'GET':
          return this.httpClient.get<T>(url);
        case 'POST':
          return this.httpClient.post<T>(url, data);
        case 'PUT':
          return this.httpClient.put<T>(url, data);
        case 'DELETE':
          return this.httpClient.delete<T>(url);
        default:
          return throwError(() => new Error('Invalid HTTP verb'));
      }
    });
  }
}

export type HTTPVerb = 'GET' | 'POST' | 'PUT' | 'DELETE';

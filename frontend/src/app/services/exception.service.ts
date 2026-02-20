import { inject, Injectable } from '@angular/core';
import { HttpService } from './http.service';
import { Observable } from 'rxjs';
import { Exception } from '../interfaces/exception';

@Injectable({
  providedIn: 'root',
})
export class ExceptionService {
  private readonly httpService = inject(HttpService);

  getExceptions(page: number): Observable<Exception[]> {
    return this.httpService.request<Exception[]>('GET', `/exceptions?page=${page}`);
  }

  deleteAll(): Observable<void> {
    return this.httpService.request<void>('POST', '/exceptions/deleteAll');
  }

  getException(exceptionId: number): Observable<Exception> {
    return this.httpService.request<Exception>(
      'GET',
      `/exception/search?exceptionId=${exceptionId}`,
    );
  }
}

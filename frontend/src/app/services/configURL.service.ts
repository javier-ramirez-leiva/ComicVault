import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ConfigURLService {
  private _baseURL: string = '/api';
  private _apiVersion: string = 'v1.0.0';

  get baseURL(): string {
    return this._baseURL;
  }

  get apiVersion(): string {
    return this._apiVersion;
  }
}

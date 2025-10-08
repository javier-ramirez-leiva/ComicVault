import { inject, Injectable } from '@angular/core';
import { LocalStorageService } from './local-storage.service';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReaderSettingsService {

  private readonly localStorageService: LocalStorageService = inject(LocalStorageService);

  private readerSettingsBooleanMap: ReaderSettingsBooleanMap;

  doublePageConfiguration$ = new BehaviorSubject<DoublePageConfiguration>('auto');
  private doublePageConfiguration: DoublePageConfiguration = 'auto';

  constructor() {
    this.readerSettingsBooleanMap = Object.fromEntries(READER_SETTINGS_BOOLEANS.map(k => [k, this.localStorageService.getItem(k) ?? true])) as ReaderSettingsBooleanMap;
    this.doublePageConfiguration = this.localStorageService.getItem('doublePageConfiguration') ?? 'auto';
    this.doublePageConfiguration$.next(this.doublePageConfiguration);
  }

  get(): ReaderSettingsBooleanMap {
    return this.readerSettingsBooleanMap;
  }

  set(value: ReaderSettingsBooleanMap) {
    this.readerSettingsBooleanMap = value;
    READER_SETTINGS_BOOLEANS.forEach(k => this.localStorageService.setItem(k, this.readerSettingsBooleanMap[k]));
  }

  getValue(valueKey: ReaderSettingsBooleans): boolean {
    return this.readerSettingsBooleanMap[valueKey];
  }

  getDoublePageConfiguration() {
    return this.doublePageConfiguration;
  }

  setValue(valueKey: ReaderSettingsBooleans, value: boolean) {
    this.readerSettingsBooleanMap[valueKey] = value;
    this.localStorageService.setItem(valueKey, value);
  }

  setDoublePageConfiguration(value: DoublePageConfiguration) {
    this.doublePageConfiguration = value;
    this.localStorageService.setItem('doublePageConfiguration', value);
    this.doublePageConfiguration$.next(this.doublePageConfiguration);
  }
}

export const READER_SETTINGS_BOOLEANS = [
  'thumbnailNavigation',
  'gestures',
  'resetZoom',
  'noMenuZoom',
] as const;

export type ReaderSettingsBooleans = typeof READER_SETTINGS_BOOLEANS[number];

export type ReaderSettingsBooleanMap = {
  [K in ReaderSettingsBooleans]: boolean;
};

export type DoublePageConfiguration = 'single' | 'double' | 'auto'

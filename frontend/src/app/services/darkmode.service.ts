import { Injectable, effect, signal, Inject, inject } from '@angular/core';
import { LocalStorageService } from './local-storage.service';
import { Observable, map } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';



@Injectable({
  providedIn: 'root'
})
export class DarkmodeService {

  darkModeSignal = signal<string>('dark');
  private readonly localStorageService: LocalStorageService = inject(LocalStorageService);
  isDarkMode$: Observable<boolean>;

  constructor() {
    this.isDarkMode$ = toObservable(this.darkModeSignal).pipe(
      map(value => value === 'dark')
    );
  }

  init() {
    const mode = this.localStorageService.getItem('darkMode');
    if (mode === 'null') {
      this.darkModeSignal.set('null');
    } else {
      this.darkModeSignal.set('dark');
    }
    this.setDarkModeHtml(mode);

  }

  updateDarkMode() {
    this.darkModeSignal.update(value => value === 'dark' ? 'null' : 'dark');
    this.localStorageService.setItem('darkMode', this.darkModeSignal());
    this.setDarkModeHtml(this.darkModeSignal());
  }

  toggleDarkMode() {
    this.darkModeSignal.update(value => value === 'dark' ? 'null' : 'dark');
    this.localStorageService.setItem('darkMode', this.darkModeSignal());
    this.setDarkModeHtml(this.darkModeSignal());
  }

  private setDarkModeHtml(value: string) {
    document.documentElement.classList.toggle('dark', value === 'dark' ? true : false);
  }
}

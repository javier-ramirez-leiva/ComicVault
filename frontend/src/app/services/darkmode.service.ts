import { Injectable, signal, inject } from '@angular/core';
import { LocalStorageService } from './local-storage.service';
import { Observable, map } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';

export const COLOR_PALETTES = [
  { id: 'ocean', label: 'Ocean' },
  { id: 'forest', label: 'Forest' },
  { id: 'violet', label: 'Violet' },
  { id: 'sunset', label: 'Sunset' },
] as const;
export type ColorPalette = (typeof COLOR_PALETTES)[number]['id'];

@Injectable({ providedIn: 'root' })
export class DarkmodeService {
  darkModeSignal = signal<string>('dark');
  private readonly paletteState = signal<ColorPalette>('ocean');
  readonly palette = this.paletteState.asReadonly();
  readonly palettes = COLOR_PALETTES;
  private readonly localStorageService = inject(LocalStorageService);
  readonly isDarkMode$: Observable<boolean> = toObservable(this.darkModeSignal).pipe(
    map((value) => value === 'dark'),
  );

  init() {
    this.darkModeSignal.set(this.readPreference('darkMode') === 'null' ? 'null' : 'dark');
    this.applyMode();
    this.applyPalette(this.readPreference('colorPalette'));
  }

  updateDarkMode() {
    this.setDarkMode(this.darkModeSignal() !== 'dark');
  }

  toggleDarkMode() {
    this.updateDarkMode();
  }

  setDarkMode(dark: boolean) {
    this.darkModeSignal.set(dark ? 'dark' : 'null');
    this.applyMode();
    this.writePreference('darkMode', this.darkModeSignal());
  }

  setPalette(value: ColorPalette) {
    this.applyPalette(value);
    this.writePreference('colorPalette', this.palette());
  }

  private applyMode() {
    document.documentElement.classList.toggle('dark', this.darkModeSignal() === 'dark');
  }

  private applyPalette(value: unknown) {
    const palette = COLOR_PALETTES.find((entry) => entry.id === value)?.id ?? 'ocean';
    this.paletteState.set(palette);
    document.documentElement.dataset['palette'] = palette;
  }

  private readPreference(key: string): unknown {
    try { return this.localStorageService.getItem(key); } catch { return null; }
  }

  private writePreference(key: string, value: string) {
    try { this.localStorageService.setItem(key, value); } catch { /* Session-only when storage is unavailable. */ }
  }
}

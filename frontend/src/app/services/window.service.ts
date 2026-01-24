import { DOCUMENT } from '@angular/common';
import { inject, Inject, Injectable } from '@angular/core';
import { Observable, debounceTime, filter, fromEvent, map, startWith } from 'rxjs';
import { NotifierService } from './notifier.service';

@Injectable({
  providedIn: 'root',
})
export class WindowService {
  readonly screenWidth$: Observable<number>;
  readonly scrollBottom$: Observable<void>;

  private readonly notifierService = inject(NotifierService);

  constructor(@Inject(DOCUMENT) private document: Document) {
    this.screenWidth$ = fromEvent(window, 'resize').pipe(
      map(() => window.innerWidth),
      startWith(window.innerWidth),
    );
    this.scrollBottom$ = fromEvent(window, 'scroll').pipe(
      map(() => {
        const scrollPosition = window.scrollY + window.innerHeight;
        const documentHeight = document.documentElement.scrollHeight;
        return scrollPosition >= documentHeight - 200;
      }),
      filter((atBottom) => atBottom),
      debounceTime(100),
      map(() => undefined),
    );
  }

  scrollToTop(position: number, scrollBehavior: ScrollBehavior): void {
    window.scrollTo({ top: position, behavior: scrollBehavior });
  }

  isZoomed(): boolean {
    return (window.visualViewport?.scale ?? 1) > 1.1;
  }

  //NOT working on pwa
  resetZoom() {
    document.body.style.transform = 'scale(1)';
    document.body.style.transformOrigin = '0 0';
    setTimeout(() => {
      document.body.style.transform = '';
    }, 0);
  }

  //NOT working on pwa
  setZoomEnabled(enabled: boolean) {
    const viewport = document.querySelector<HTMLMetaElement>('meta[name=viewport]');
    if (viewport) {
      if (enabled) {
        viewport.setAttribute(
          'content',
          'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes',
        );
      } else {
        viewport.setAttribute(
          'content',
          'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no',
        );
      }
    }
  }
}

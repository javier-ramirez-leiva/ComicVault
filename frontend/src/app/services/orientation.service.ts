import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { fromEvent, map, Observable, startWith } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class OrientationService {
  orientationChange$: Observable<Orientation>;

  constructor(@Inject(DOCUMENT) private document: Document) {
    // Use resize, works everywhere, and also covers orientationchange
    this.orientationChange$ = fromEvent(window, 'resize').pipe(map(() => this.getOrientation()));
  }

  public getOrientation(): Orientation {
    // matchMedia is reliable across browsers
    return window.matchMedia('(orientation: portrait)').matches ? 'portrait' : 'landscape';
  }
}

export type Orientation = 'landscape' | 'portrait';

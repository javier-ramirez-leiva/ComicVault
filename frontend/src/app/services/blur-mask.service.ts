import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class BlurMaskService {
  active = false;

  constructor() {}

  setActive(value: boolean) {
    this.active = value;
  }
}

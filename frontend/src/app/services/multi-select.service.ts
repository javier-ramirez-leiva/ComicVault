import { Injectable, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

@UntilDestroy()
export class MultiSelectService {

  private _refresh$ = new Subject<void>();
  refresh$ = this._refresh$.asObservable();

  refresh(): void {
    this._refresh$.next();
  }

}

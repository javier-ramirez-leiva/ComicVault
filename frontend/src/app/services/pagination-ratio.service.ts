import { Injectable, inject } from '@angular/core';
import { LocalStorageService } from './local-storage.service';

@Injectable({
  providedIn: 'root',
})
export class PaginationRatioService {
  private readonly localStorageService: LocalStorageService = inject(LocalStorageService);

  private paginationRatio: number = 1;

  constructor() {
    const ratio = this.localStorageService.getItem('paginationRatio');
    this.paginationRatio = ratio ? parseInt(ratio, 10) : 1;
  }

  getPaginationRatio(): number {
    return this.paginationRatio;
  }

  setPaginationRatio(ratio: number) {
    this.paginationRatio = ratio;
    this.localStorageService.setItem('paginationRatio', this.paginationRatio.toString());
    return this.paginationRatio;
  }
}

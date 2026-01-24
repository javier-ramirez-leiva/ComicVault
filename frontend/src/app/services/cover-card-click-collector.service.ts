import { Injectable, inject } from '@angular/core';
import { CoverCardComponent } from 'components';
import { BehaviorSubject, filter } from 'rxjs';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { notNullOrUndefined } from '../utils/rsjx-operators';
import { isNonResetRoute, isStoredRoute } from '../strategy_providers/custom-reuse-strategy';
import { ComicCard } from 'interfaces';

@Injectable({
  providedIn: 'root',
})
@UntilDestroy()
export class CoverCardClickCollectorService {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  private comicCards: ComicCard[] = [];
  private multiSelect: boolean = false;
  public multiSelect$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor() {
    this.router.events
      .pipe(
        untilDestroyed(this),
        filter((event) => event instanceof NavigationEnd),
      )
      .subscribe((_) => {
        let currentRoute = this.route;
        while (currentRoute.firstChild) {
          currentRoute = currentRoute.firstChild;
        }
        const routePath = currentRoute.routeConfig?.path;
        if (routePath && !isNonResetRoute(routePath) && !isStoredRoute(routePath)) {
          this.multiSelect = false;
          this.comicCards.forEach((cardID) => this.removeActiveHover);
          this.comicCards = [];
          this.comicCards = [];
        }
      });
  }

  setActiveHover(cardID: string): void {
    for (let comicCard of this.comicCards) {
      if (comicCard.getComic().id === cardID) {
        comicCard.setHovered(true);
      } else {
        comicCard.setHovered(false);
      }
    }
    this.comicCards = [];
  }

  pushActiveHover(comicCard: ComicCard): void {
    if (!this.comicCards.find((cc) => comicCard.getComic().id == cc.getComic().id)) {
      this.comicCards.push(comicCard);
    }
  }

  removeActiveHover(cardID: string): void {
    this.comicCards = this.comicCards.filter((comicCard) => comicCard.getComic().id !== cardID);
  }

  clearActiveHovers() {
    this.comicCards.forEach((comicCard) => comicCard.setHovered(false));
    this.comicCards = [];
  }

  isCardActiveHover(cardID: string): boolean {
    return this.comicCards.map((comicCard) => comicCard.getComic().id).includes(cardID);
  }

  setMultiSelect(value: boolean): void {
    this.multiSelect = value;
    this.comicCards.forEach((comicCard) => comicCard.setHovered(false));
    this.comicCards = [];
  }

  getMultiSelect(): boolean {
    return this.multiSelect;
  }

  getActiveCards(): ComicCard[] {
    return this.comicCards;
  }
}

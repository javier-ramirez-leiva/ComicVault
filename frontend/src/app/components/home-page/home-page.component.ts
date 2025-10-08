import { Component, inject } from '@angular/core';
import { ComicsService } from 'services';
import { Observable, combineLatest, map } from 'rxjs';
import { ComicsDatabase, Series } from 'interfaces';
import { CommonModule } from '@angular/common';
import { UntilDestroy } from '@ngneat/until-destroy';
import { NoResultsComponent } from "../no-results/no-results.component";
import { LoadingSpinnerPageComponent } from "../loading-spinner-page/loading-spinner-page.component";
import { CarouselSeriesComicsComponent } from "../carousel-series-comics/carousel-series-comics.component";

@Component({
  selector: 'app-home-page',
  imports: [CommonModule, NoResultsComponent, LoadingSpinnerPageComponent, CarouselSeriesComicsComponent],
  templateUrl: './home-page.component.html',
})

@UntilDestroy()
export class HomePageComponent {

  public homePageData$: Observable<HomePageData>;

  comicService: ComicsService = inject(ComicsService);

  constructor() {
    this.homePageData$ = combineLatest([
      this.comicService.ongoingComics(),
      this.comicService.ongoingSeries(),
      this.comicService.newComics(),
      this.comicService.newSeries()
    ]).pipe(
      map(([ongoingComics, ongoingSeries, newComics, newSeries]) => ({
        ongoingComics,
        ongoingSeries,
        newComics,
        newSeries
      }))
    );
  }

}

type HomePageData = {
  ongoingComics: ComicsDatabase[];
  ongoingSeries: Series[];
  newComics: ComicsDatabase[];
  newSeries: Series[];
}

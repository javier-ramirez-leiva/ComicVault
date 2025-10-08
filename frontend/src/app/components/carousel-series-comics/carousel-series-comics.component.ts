import { Component, ElementRef, Input, OnInit, QueryList, ViewChild, ViewChildren, inject } from '@angular/core';
import { ComicsDatabase, ComicsSearch, Series, Tag } from 'interfaces';
import { WindowService } from 'services';
import { Observable, fromEvent, map, merge, shareReplay, startWith } from 'rxjs';
import { SeriesComponent } from "../series/series.component";
import { CommonModule } from '@angular/common';
import { ComicDatabaseComponent } from "../comic-database/comic-database.component";
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { ComicSearchComponent } from '../comic-search/comic-search.component';

@UntilDestroy()
@Component({
  selector: 'app-carousel-series-comics',
  imports: [SeriesComponent, CommonModule, ComicDatabaseComponent, ComicSearchComponent],
  templateUrl: './carousel-series-comics.component.html'
})
export class CarouselSeriesComicsComponent implements OnInit {
  @Input({ required: false }) public series: Series[] = [];;
  @Input({ required: false }) public comics: ComicsDatabase[] = [];
  @Input({ required: false }) public comicSearchs: ComicsSearch[] = [];
  @Input({ required: false }) public header: string | undefined = undefined;
  @Input({ required: false }) public scrollToIndex$: Observable<number> | undefined;
  @Input({ required: false }) public tag: Tag | null = null;

  protected serializedTag: string | null = null;

  @ViewChild('scrollContainer') scrollContainer!: ElementRef;
  @ViewChildren('comicItem') comicItems!: QueryList<ElementRef>;

  private readonly windowService = inject(WindowService);

  protected canScrollLeft$!: Observable<boolean>;
  protected canScrollRight$!: Observable<boolean>;

  trackByComic(index: number, comic: ComicsDatabase): string {
    return comic.id;
  }

  trackByComicSearch(index: number, comic: ComicsSearch): string {
    return comic.idGc;
  }

  trackBySeries(index: number, series: Series): string {
    return series.id;
  }

  ngOnInit() {
    if (this.scrollToIndex$) {
      this.scrollToIndex$.pipe(
        untilDestroyed(this)
      ).subscribe(index => {
        this.scrollToComic(index);
      });
    }
    if (this.tag) {
      this.serializedTag = JSON.stringify(this.tag);
    }
  }

  ngAfterViewInit(): void {
    const scrollEl = this.scrollContainer.nativeElement;

    const scroll$ = fromEvent(scrollEl, 'scroll');
    const resize$ = fromEvent(window, 'resize');

    const update$ = merge(scroll$, resize$).pipe(
      startWith(null),
      map(() => ({
        left: scrollEl.scrollLeft > 0,
        right: scrollEl.scrollLeft + scrollEl.clientWidth < scrollEl.scrollWidth
      }))
    );

    this.canScrollLeft$ = update$.pipe(
      map(state => state.left),
      shareReplay({ bufferSize: 1, refCount: true })
    );
    this.canScrollRight$ = update$.pipe(
      map(state => state.right),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  private scrollToComic(index: number) {
    const container = this.scrollContainer.nativeElement;
    const comicElements = this.comicItems.toArray();

    if (index === 0 || index > comicElements.length) {
      return;
    }

    const comicElement = comicElements[index].nativeElement;

    const containerWidth = container.offsetWidth;
    const comicWidth = comicElement.offsetWidth;
    const scrollLeft = comicElement.offsetLeft - (containerWidth / 2) + (comicWidth / 2);

    container.scrollTo({
      left: scrollLeft,
      behavior: 'smooth'
    });
  }

  protected scrollFullScreen(direction: 'right' | 'left') {
    const container = this.scrollContainer.nativeElement;
    const containerWidth = container.offsetWidth;
    let scrollPosition: number;
    if (direction === 'right') {
      scrollPosition = container.scrollLeft + containerWidth;
    } else {
      scrollPosition = container.scrollLeft - containerWidth;
    }

    container.scrollTo({
      left: scrollPosition,
      behavior: 'smooth'
    });
  }
}

import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, inject, Input, ViewChild } from '@angular/core';
import { fromEvent, map, merge, Observable, shareReplay, startWith } from 'rxjs';
import { WindowService } from 'services';

@Component({
  selector: 'app-carousel-screenshots',
  imports: [CommonModule],
  templateUrl: './carousel-screenshots.component.html',
})
export class CarouselScreenshotsComponent implements AfterViewInit {
  @Input({ required: true }) public coverUrls: string[] = [];

  @ViewChild('scrollContainer') scrollContainer!: ElementRef;

  private readonly windowService = inject(WindowService);

  protected canScrollLeft$!: Observable<boolean>;
  protected canScrollRight$!: Observable<boolean>;

  ngAfterViewInit(): void {
    const scrollEl = this.scrollContainer.nativeElement;

    const scroll$ = fromEvent(scrollEl, 'scroll');
    const resize$ = fromEvent(window, 'resize');

    const update$ = merge(scroll$, resize$).pipe(
      startWith(null),
      map(() => ({
        left: scrollEl.scrollLeft > 0,
        right: scrollEl.scrollLeft + scrollEl.clientWidth < scrollEl.scrollWidth,
      })),
    );

    this.canScrollLeft$ = update$.pipe(
      map((state) => state.left),
      shareReplay({ bufferSize: 1, refCount: true }),
    );
    this.canScrollRight$ = update$.pipe(
      map((state) => state.right),
      shareReplay({ bufferSize: 1, refCount: true }),
    );
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
      behavior: 'smooth',
    });
  }
}

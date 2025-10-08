import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, inject, Input, OnInit, Output, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { ComicsDatabase } from 'interfaces';
import { ConfigURLService } from 'services';
import { range } from 'src/app/utils/number';


@Component({
  selector: 'app-minipages-carousel',
  imports: [CommonModule],
  templateUrl: './minipages-carousel.component.html'
})
export class MinipagesCarouselComponent implements OnInit {
  @Input({ required: true }) comic!: ComicsDatabase;
  @Output() pageChange = new EventEmitter<number>();

  private readonly configURLService = inject(ConfigURLService);

  protected pagesURL: string = "";
  protected widthStyle: string = '';

  protected imageErrors: boolean[] = [];

  range: number[][] = [];

  @ViewChild('scrollContainer') scrollContainer!: ElementRef;
  @ViewChildren('image') comicItems!: QueryList<ElementRef>;

  ngOnInit(): void {
    this.pagesURL = `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${this.comic.id}/minipages/`;
    this.imageErrors = Array(this.comic.pages).fill(false);
  }


  private _visible = false;

  @Input({ required: true })
  set visible(value: boolean) {
    this._visible = value;
    if (value) {
      setTimeout(() => this.scrollToImage(this._page, true), 100);
    }
  }

  get visible(): boolean {
    return this._visible;
  }

  private _page = 0;

  @Input({ required: true })
  set page(value: number) {
    this._page = value;
    const progress = 100 * (this._page / this.comic.pages);
    this.widthStyle = `width: ${progress}%`;
    setTimeout(() => this.scrollToImage(this._page, true), 100);
  }

  get page(): number {
    return this._page;
  }

  private _doublePages = false;

  @Input({ required: true })
  set doublePages(value: boolean) {
    this._doublePages = value;
    this.range = computeDoublePages(this.comic, this._doublePages);
  }

  get doublePages(): boolean {
    return this._doublePages;
  }

  scrollToImage(index: number, largeDistance: boolean) {
    const container = this.scrollContainer.nativeElement;
    let cardElement = container.querySelector(`#card-${index}`);

    if (!cardElement) {
      cardElement = container.querySelector(`#card-${index - 1}`);
    }

    if (!cardElement) return;

    cardElement.scrollIntoView({
      behavior: largeDistance ? 'auto' : 'smooth',
      block: 'nearest',    // vertical alignment (irrelevant for horizontal scrolling)
      inline: 'center'     // horizontal alignment: center of container
    });

    // Fallback: ensure it's visible after animation ends
    /*if (largeDistance) {
      setTimeout(() => {
        imageElement.scrollIntoView({ behavior: 'auto', inline: 'center', block: 'nearest' });
      }, 600);
    }*/


  }


  emitPageChange(index: number) {
    const progress = 100 * (index / this.comic.pages);
    this.widthStyle = `width: ${progress}%`;
    setTimeout(() => this.scrollToImage(index, false));
    this.pageChange.emit(index);
  }

  isIndexesActive(indexes: number[]) {
    return this.page === indexes[0] || (indexes.length > 1 && this.page === indexes[1])
  }

  getPageText(indexes: number[]): string {
    return indexes.length > 1 ? `${indexes[0]} - ${indexes[1]}` : `${indexes[0]}`;
  }

}

export function computeDoublePages(comic: ComicsDatabase, doublePages: boolean): number[][] {
  const range = [];
  for (let i = 0; i < comic.pages; ++i) {
    if (!doublePages) {
      range.push([i]);
    } else {
      if (comic.doublePages.includes(i) || comic.doublePages.includes(i + 1)) {
        range.push([i]);
      }
      else if (i === 0 && !comic.doublePageCover) {
        range.push([i]);
      } else if (i === comic.pages - 1) {
        range.push([i]);
      } else {
        range.push([i, i + 1]);
        ++i;
      }
    }
  }
  return range;
}

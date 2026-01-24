import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ComicCard, ComicsDatabase } from 'interfaces';
import { ConfigURLService, CoverCardClickCollectorService, MultiSelectService } from 'services';
import { PublisherCellComponent } from '../publisher-cell/publisher-cell.component';
import {
  CdkDrag,
  CdkDragDrop,
  CdkDragHandle,
  CdkDropList,
  moveItemInArray,
} from '@angular/cdk/drag-drop';
import { StatusCellComponent } from '../status-cell/status-cell.component';
import { BooleanCheckboxComponent } from '../boolean-checkbox/boolean-checkbox.component';

@Component({
  selector: 'app-comic-database-table',
  imports: [
    CommonModule,
    RouterLink,
    PublisherCellComponent,
    CdkDropList,
    CdkDrag,
    CdkDragHandle,
    StatusCellComponent,
    BooleanCheckboxComponent,
  ],
  templateUrl: './comic-database-table.component.html',
})
export class ComicDatabaseTableComponent {
  @Input({ required: false }) editStatus = false;

  @Output() onComicsChange = new EventEmitter<ComicsDatabase[]>();

  protected readonly configURLService = inject(ConfigURLService);
  protected readonly coverCardClickCollector = inject(CoverCardClickCollectorService);
  private listIssues: number[] = [];
  protected rowComics: RowComic[] = [];

  private _comics: ComicsDatabase[] = [];
  @Input({ required: true })
  set comics(comics: ComicsDatabase[]) {
    this._comics = comics;
    this.listIssues = this._comics.map((comic) => comic.issue);

    this.rowComics = this._comics.map(
      (comic) => new RowComic(comic, this.coverCardClickCollector.isCardActiveHover(comic.id)),
    );
  }

  get comics(): ComicsDatabase[] {
    return this._comics;
  }

  drop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.rowComics, event.previousIndex, event.currentIndex);
    moveItemInArray(this._comics, event.previousIndex, event.currentIndex);
    for (let i = 0; i < this.rowComics.length; ++i) {
      this.rowComics[i].comic.issue = this.listIssues[i];
    }
    this.onComicsChange.emit(this._comics);
  }

  trackByComicId(index: number, rowComic: RowComic) {
    return rowComic.comic.id;
  }

  onClickRow(rowComic: RowComic) {
    if (this.coverCardClickCollector.getMultiSelect()) {
      rowComic.toggleHover();
      if (rowComic.hovered) {
        this.coverCardClickCollector.pushActiveHover(rowComic);
      } else {
        this.coverCardClickCollector.removeActiveHover(rowComic.comic.id);
      }
    }
  }

  onClickRowCheckbox(rowComic: RowComic, value: boolean) {
    if (this.coverCardClickCollector.getMultiSelect()) {
      rowComic.setHovered(value);
      if (rowComic.hovered) {
        this.coverCardClickCollector.pushActiveHover(rowComic);
      } else {
        this.coverCardClickCollector.removeActiveHover(rowComic.comic.id);
      }
    }
  }
}

class RowComic implements ComicCard {
  public comic: ComicsDatabase;
  public hovered = false;
  constructor(comic: ComicsDatabase, isHovered: boolean) {
    this.comic = comic;
    this.hovered = isHovered;
  }

  setHovered(isHovered: boolean) {
    this.hovered = isHovered;
  }

  toggleHover() {
    this.hovered = !this.hovered;
  }

  getComic(): ComicsDatabase {
    return this.comic;
  }
}

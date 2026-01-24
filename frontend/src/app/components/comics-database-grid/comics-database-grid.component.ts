import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { ComicsDatabase } from 'interfaces';
import { ComicDatabaseComponent } from '../comic-database/comic-database.component';

import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import {
  CdkDrag,
  CdkDragDrop,
  CdkDragHandle,
  CdkDragPreview,
  CdkDropList,
  moveItemInArray,
} from '@angular/cdk/drag-drop';
import { ConfigURLService, CoverCardClickCollectorService } from 'services';

@Component({
  selector: 'app-comics-database-grid',
  imports: [
    ComicDatabaseComponent,
    CdkDropList,
    CdkDrag,
    CdkDragHandle,
    CdkDragPreview
],
  styleUrls: ['./comics-database-grid.component.css'],
  templateUrl: './comics-database-grid.component.html',
})
@UntilDestroy()
export class ComicsDatabaseGridComponent implements OnInit {
  @Input({ required: false }) editStatus = false;

  @Output() onComicsChange = new EventEmitter<ComicsDatabase[]>();

  private readonly coverCardClickCollector = inject(CoverCardClickCollectorService);

  private listIssues: number[] = [];
  protected comicsAndHovers: ComicAndHover[] = [];
  dragging = false;

  private readonly configURLService = inject(ConfigURLService);

  private _comics: ComicsDatabase[] = [];
  @Input({ required: true })
  set comics(comics: ComicsDatabase[]) {
    this._comics = comics;
    this.listIssues = this._comics.map((comic) => comic.issue);
    this.comicsAndHovers = this._comics.map((comic) => ({
      comic,
      isHovered: this.coverCardClickCollector.isCardActiveHover(comic.id),
    }));
  }

  get comics(): ComicsDatabase[] {
    return this._comics;
  }

  ngOnInit(): void {}

  trackByComic(index: number, comicAndHover: ComicAndHover): string {
    return comicAndHover.comic.id;
  }

  drop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.comicsAndHovers, event.previousIndex, event.currentIndex);
    moveItemInArray(this._comics, event.previousIndex, event.currentIndex);
    for (let i = 0; i < this.comicsAndHovers.length; ++i) {
      this.comicsAndHovers[i].comic.issue = this.listIssues[i];
    }
    this.onComicsChange.emit(this._comics);
  }

  dragStart() {
    this.dragging = true;
  }
  dradgEnd() {
    this.dragging = false;
  }

  getComicImageUrl(comicId: string): string {
    return `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${comicId}/cover/small`;
  }
}

type ComicAndHover = {
  comic: ComicsDatabase;
  isHovered: boolean;
};

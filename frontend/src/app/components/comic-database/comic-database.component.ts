import { Component, Input, OnInit, inject } from '@angular/core';
import { ComicsDatabase } from 'interfaces';
import { CoverCardComponent } from '../cover-card/cover-card.component';
import { ConfigURLService } from 'services';

@Component({
  selector: 'app-comic-database',
  imports: [CoverCardComponent],
  templateUrl: './comic-database.component.html',
})
export class ComicDatabaseComponent {
  @Input({ required: false }) public highlight: boolean = false;
  @Input({ required: false }) public draggableStatus: boolean = false;
  @Input({ required: false }) public isHovered: boolean = false;

  imageURL!: string;
  private readonly configURLService = inject(ConfigURLService);
  url: string[] = [];
  progress: number | undefined = undefined;
  new: boolean = false;
  secondLine: string = '';
  firstLine: string = '';

  private _comic!: ComicsDatabase;
  @Input({ required: true })
  set comic(comic: ComicsDatabase) {
    this._comic = comic;
    this.imageURL = `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${this._comic.id}/cover/small`;
    this.url = ['/comics', this._comic.id, 'details'];
    this.new = this._comic.pageStatus === 0 && !this._comic.readStatus;
    this.progress =
      !this.new && !this._comic.readStatus
        ? 100 * (this._comic.pageStatus / this._comic.pages)
        : undefined;
    this.firstLine = 'Pages: ' + this.comic.pages;
    if (this._comic.readStatus) {
      this.secondLine = 'Status: READ';
    } else if (this._comic.pageStatus === 0) {
      this.secondLine = 'Status: NEW';
    } else {
      this.secondLine = `Status: ${this._comic.pageStatus} / ${this._comic.pages}`;
    }
  }

  get comic(): ComicsDatabase {
    return this._comic;
  }
}

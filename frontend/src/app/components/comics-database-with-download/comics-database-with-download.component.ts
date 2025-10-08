import { Component, Input, inject } from '@angular/core';
import { CoverCardComponent } from '../cover-card/cover-card.component';
import { ComicsDatabase } from 'interfaces';
import { ConfigURLService } from 'services';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-comics-database-with-download',
  imports: [CoverCardComponent],
  templateUrl: './comics-database-with-download.component.html',
})
export class ComicsDatabaseWithDownloadComponent {
  @Input({ required: true }) public comic!: ComicsDatabase;
  @Input({ required: true }) public progress$!: Observable<number>;
  @Input() public highlight: boolean = false;

  imageURL!: string;
  private readonly configURLService = inject(ConfigURLService);
  url: string[] = [];
  progress: number | undefined = undefined;

  ngOnInit(): void {
    this.imageURL = `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${this.comic.id}/cover/small`;
    this.url = ['/comics', this.comic.id, 'details'];
    this.progress = 100 * (this.comic.pageStatus / this.comic.pages);
  }
}

import { Component, Input, WritableSignal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HideRolesDirective, OutsideClickDirective } from 'directives';
import { CoverCardClickCollectorService } from 'services';
import { DownloadButtonComponent } from '../download-button/download-button.component';
import { ComicCard, ComicsDatabase, ComicsSearch, Role } from 'interfaces';
import { Observable, filter } from 'rxjs';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';


@UntilDestroy()
@Component({
  selector: 'app-cover-card',
  imports: [CommonModule, OutsideClickDirective, DownloadButtonComponent, HideRolesDirective],
  templateUrl: './cover-card.component.html',
})
export class CoverCardComponent implements ComicCard {

  @Input() public highlight: boolean = false;
  @Input({ required: true }) public id!: string;
  @Input({ required: true }) public imageURL!: string;
  @Input({ required: true }) public navigationURL!: string[];
  @Input({ required: true }) public title!: string;
  @Input({ required: true }) public firstLine!: string;
  @Input({ required: true }) public secondLine!: string;
  @Input({ required: true }) public searchOrDatabase!: boolean;
  @Input() public progress: number | undefined = undefined;
  @Input() public progress$: Observable<number> | undefined = undefined;
  @Input() public checked: boolean = false;
  @Input() public new: boolean = false;
  @Input() public draggableStatus: boolean = false;
  @Input() public numberBadge$: Observable<number | null> | undefined = undefined;
  @Input() public numberBadge: number | undefined = undefined;
  @Input() comicsDatabase: ComicsDatabase | undefined;
  @Input() comicsSearch: ComicsSearch | undefined;
  @Input() displayButton: boolean = true;
  @Input() queryParams: any = null;
  @Input() public issueLine: string = '';
  @Input() public isHovered: boolean = false;

  private readonly router: Router = inject(Router);
  widthStyle: string | null = null;
  imageLoaded: boolean = false;
  isTouchDevice: boolean = false;
  Role = Role;

  coverCardClickCollectorService: CoverCardClickCollectorService = inject(CoverCardClickCollectorService);

  getComic(): ComicsDatabase {
    if (!this.comicsDatabase) {
      throw new Error('Should not happen');
    }
    return this.comicsDatabase;
  }

  setHovered(value: boolean) {
    this.isHovered = value;
  }

  constructor() {
    this.isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
  }

  ngOnInit(): void {
    if (this.progress$) {
      this.progress$.pipe(
        untilDestroyed(this)
      ).subscribe(progress => {
        this.widthStyle = progress > 0 ? `width: ${progress}%` : null;
      });
    } else {
      if (this.progress && this.progress > 0) {
        this.widthStyle = this.progress > 0 ? `width: ${this.progress}%` : null;
      }
    }
  }

  onCardClick(event: Event): void {
    if (this.isTouchDevice) {
      if (this.isHovered === true) {
        this.isHovered = false;
        if (this.coverCardClickCollectorService.getMultiSelect()) {
          this.coverCardClickCollectorService.removeActiveHover(this.id);
        } else {
          if (this.queryParams) {
            this.router.navigate(this.navigationURL, { queryParams: this.queryParams, queryParamsHandling: 'merge' });
          } else {
            this.router.navigate(this.navigationURL);
          }
        }
      } else {
        this.isHovered = true;
        if (this.coverCardClickCollectorService.getMultiSelect()) {
          this.coverCardClickCollectorService.pushActiveHover(this);
        } else {
          this.coverCardClickCollectorService.setActiveHover(this.id);
        }
      }
    } else {
      if (this.coverCardClickCollectorService.getMultiSelect()) {
        if (this.coverCardClickCollectorService.isCardActiveHover(this.id)) {
          this.coverCardClickCollectorService.removeActiveHover(this.id);
        } else {
          this.coverCardClickCollectorService.pushActiveHover(this);
        }
      } else {
        if (this.queryParams) {
          this.router.navigate(this.navigationURL, { queryParams: this.queryParams, queryParamsHandling: 'merge' });
        } else {
          this.router.navigate(this.navigationURL);
        }
      }
    }
    event.preventDefault();
    event.stopPropagation();
  }

  onHover(value: boolean): void {
    if (!this.isTouchDevice) {
      if (!this.coverCardClickCollectorService.isCardActiveHover(this.id)) {
        this.isHovered = value;
      }
    }
  }

  hoverFalse() {
    if (!this.coverCardClickCollectorService.getMultiSelect()) {
      this.isHovered = false;
    }
  }

  onDownload() {
    this.displayButton = false;
    this.progress = 0;
  }
}
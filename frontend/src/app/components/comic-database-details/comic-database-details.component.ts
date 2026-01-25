import { Component, OnInit, inject } from '@angular/core';
import { Category, ComicsDatabase, HttpResponseError, Series } from 'interfaces';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ComicsService, ModalService, NotifierService } from 'services';
import { ReadButtonComponent } from '../read-button/read-button.component';
import { ConfigURLService } from 'services';
import { DeleteButtonComponent } from '../delete-button/delete-button.component';
import { SeenButtonComponent } from '../seen-button/seen-button.component';
import { Router } from '@angular/router';
import {
  catchError,
  combineLatest,
  EMPTY,
  filter,
  map,
  Observable,
  of,
  shareReplay,
  startWith,
  Subject,
  switchMap,
  tap,
} from 'rxjs';
import { CommonModule } from '@angular/common';
import { DownloadFileButtonComponent } from '../download-file-button/download-file-button.component';
import { HideRolesDirective } from 'directives';
import { Role } from 'interfaces';
import { LoadingSpinnerPageComponent } from '../loading-spinner-page/loading-spinner-page.component';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { ComicNotFoundComponent } from '../comic-not-found/comic-not-found.component';
import { CarouselSeriesComicsComponent } from '../carousel-series-comics/carousel-series-comics.component';
import { Row, TwoColumnsTableComponent } from '../two-columns-table/two-columns-table.component';
import { WindowService } from 'services';
import { TagChipComponent } from '../tag-chip/tag-chip.component';
import { EditMetadataButtonComponent } from '../edit-metadata-button/edit-metadata-button.component';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { InputTextFormComponent } from '../input-text-form/input-text-form.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';
import { ModalMiniPagesComponent } from '../modal-mini-pages/modal-mini-pages.component';
import { CanComponentDeactivate } from 'src/app/guard/unsaved-changes-guard.guard';

@Component({
  selector: 'app-comic-database-details',
  imports: [
    ReadButtonComponent,
    DeleteButtonComponent,
    SeenButtonComponent,
    DownloadFileButtonComponent,
    CommonModule,
    RouterModule,
    HideRolesDirective,
    CarouselSeriesComicsComponent,
    LoadingSpinnerPageComponent,
    ComicNotFoundComponent,
    TwoColumnsTableComponent,
    TagChipComponent,
    EditMetadataButtonComponent,
    ReactiveFormsModule,
    InputTextFormComponent,
  ],
  templateUrl: './comic-database-details.component.html',
})
@UntilDestroy()
export class ComicDatabaseDetailsComponent implements OnInit, CanComponentDeactivate {
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly comicsService = inject(ComicsService);
  private readonly configURLService = inject(ConfigURLService);
  private readonly router = inject(Router);
  private readonly windowService = inject(WindowService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifierService = inject(NotifierService);
  private readonly modalService = inject(ModalService);
  private id: string | undefined;
  private comic: ComicsDatabase | undefined;
  private readonly id$: Observable<string>;
  private readonly triggerFetch$ = new Subject<void>();
  private readonly tempPageCover$ = new Subject<string>();
  private tempPageNumber = 0;
  protected readonly notFoundID$ = new Subject<string>();
  protected readonly comic$: Observable<ComicsDatabase | undefined>;
  protected readonly series$: Observable<Series>;
  protected readonly comicsSeries$: Observable<ComicsDatabase[]>;
  protected readonly imageURL$: Observable<string>;
  protected readonly widthStyle$: Observable<string>;
  protected readonly scrollToIndex$: Subject<number> = new Subject<number>();
  protected readonly rows$: Observable<Row[]>;
  protected editStatus: boolean = false;
  form: FormGroup;

  imageLoaded: boolean = false;

  Role = Role;

  constructor() {
    this.form = this.formBuilder.group({
      table: this.formBuilder.group({
        title: new FormControl(''),
        year: new FormControl(''),
        issue: new FormControl(0),
        link: new FormControl(''),
      }),
      description: new FormControl(''),
    });

    this.id$ = this.route.params.pipe(
      map((params) => params['id']),
      tap((id) => {
        this.id = id;
        this.editStatus = false;
      }),
    );
    this.comic$ = combineLatest([this.id$, this.triggerFetch$.pipe(startWith(null))]).pipe(
      switchMap(([id, _]) => this.comicsService.getComic(id)),
      catchError((error) => {
        this.notFoundID$.next(this.id ?? '');
        return of(undefined);
      }),
      tap((comic) => (this.comic = comic)),
      tap((comic) =>
        this.form.patchValue({
          table: { title: comic?.title, year: comic?.year, issue: comic?.issue, link: comic?.link },
          description: comic?.description,
        }),
      ),
      tap(() => this.tempPageCover$.next('')),
      shareReplay({ bufferSize: 1, refCount: true }),
    );
    this.series$ = this.comic$.pipe(
      filter(notNullOrUndefined()),
      switchMap((comic) => this.comicsService.getSeriesByID(comic.seriesID)),
      shareReplay({ bufferSize: 1, refCount: true }),
    );
    this.comicsSeries$ = this.series$.pipe(
      map((series) => series.comics),
      tap((comics) => {
        for (let i = 0; i < comics.length; i++) {
          if (comics[i].id === this.id) {
            comics[i] = this.comic ?? comics[i];
            comics[i].highlight = true;
            // Give some time to start the scrolling animation
            setTimeout(() => {
              this.scrollToIndex$.next(i);
            }, 500);
          }
        }
      }),
    );
    this.imageURL$ = combineLatest([this.comic$, this.tempPageCover$.pipe(startWith(''))]).pipe(
      map(([comic, tempPage]) => {
        if (tempPage.trim().length > 0) {
          return tempPage;
        } else if (comic) {
          return `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${comic.id}/cover/medium`;
        }
        //SHOULD NEVER HAPPEN
        return '';
      }),
    );
    this.widthStyle$ = this.comic$.pipe(
      filter(notNullOrUndefined()),
      map((comic) => {
        const progress = 100 * (comic.pageStatus / comic.pages);
        return `width: ${progress}%`;
      }),
    );
    this.rows$ = this.comic$.pipe(
      filter(notNullOrUndefined()),
      map((comic) => [
        {
          title: 'Title',
          type: 'editable-text',
          formControlName: 'title',
          text: comic.title,
        },
        {
          title: 'Series',
          type: 'routerLink',
          text: comic.seriesTitle,
          routerLink: ['/series', comic.seriesID, 'details'],
        },
        fromCategoryToRow(comic.category),
        {
          title: 'Issue',
          type: 'editable-number',
          formControlName: 'issue',
          number: comic.issue,
        },
        {
          title: 'Year',
          type: 'editable-text',
          formControlName: 'year',
          text: comic.year.trim().length > 0 ? comic.year : 'N/A',
        },
        {
          title: 'Size',
          type: 'text',
          text: comic.size,
        },
        {
          title: 'Pages',
          type: 'text',
          text: comic.pages.toString(),
        },
        getPagesStatus(comic),
        {
          title: 'Path',
          type: 'text',
          text: comic.path,
        },
        {
          title: 'Link',
          type: 'editable-link',
          formControlName: 'link',
          link: comic.link,
        },
      ]),
    );
  }

  get table(): FormGroup {
    return this.form.get('table') as FormGroup;
  }

  ngOnInit(): void {
    this.windowService.scrollToTop(0, 'smooth');
  }

  read() {
    if (this.comic) {
      const routerLink = ['/comics', this.comic.id, 'read'];
      this.router.navigate(routerLink);
    }
  }

  onEdit() {
    this.editStatus = true;
  }

  onSave() {
    this.editStatus = false;
    if (this.comic) {
      const value = this.form.getRawValue();
      const properties = {
        description: value.description,
        year: value.table.year,
        title: value.table.title,
        issue: value.table.issue,
        link: value.table.link,
      };
      this.comicsService
        .setComicProperties(this.comic.id, properties)
        .pipe(
          catchError((errorResponse) => {
            const error: HttpResponseError = errorResponse.error;
            this.notifierService.appendNotification({
              id: 0,
              title: 'Error setting metadata: ',
              message: error.message,
              type: 'error',
            });
            return EMPTY;
          }),
          switchMap(() => this.comicsService.setCover(this.comic?.id ?? '', this.tempPageNumber)),
          untilDestroyed(this),
        )
        .subscribe(() => {
          this.triggerFetch$.next();
          this.notifierService.appendNotification({
            id: 0,
            title: 'Updated metadata: ',
            message: this.comic?.title ?? '',
            type: 'success',
          });
        });
    }
  }

  onCancel() {
    this.editStatus = false;
    //trigger get of the original value
    this.tempPageCover$.next('');
  }

  toggleSeen(): void {
    if (this.comic) {
      this.comicsService
        .setComicListReadStatus([this.comic.id], !this.comic.readStatus)
        .pipe(untilDestroyed(this))
        .subscribe(() => {
          if (this.comic) {
            this.comic.readStatus = !this.comic.readStatus;
          }
          resetRouteCache();
          this.triggerFetch$.next();
        });
    }
  }

  displayMiniCardModal() {
    if (this.comic) {
      this.modalService
        .open<number, { comic: ComicsDatabase }>(ModalMiniPagesComponent, { comic: this.comic })
        .pipe(filter(notNullOrUndefined()), untilDestroyed(this))
        .subscribe((page) => {
          this.tempPageNumber = page;
          const url = `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/comics/${this.comic?.id}/pages/${page}`;
          this.tempPageCover$.next(url);
        });
    }
  }

  canDeactivate(): boolean {
    if (this.editStatus) {
      return confirm('Are you sure you want to leave without saving?');
    }
    return true;
  }
}

export function getPagesStatus(comic: ComicsDatabase): Row {
  const title = 'Status';
  if (comic.readStatus) {
    return {
      title: title,
      type: 'chip',
      text: 'READ',
      severity: 'Success',
    };
  }
  if (comic.pageStatus === 0) {
    return {
      title: title,
      type: 'chip',
      text: 'NEW',
      severity: 'Warning',
    };
  }
  return {
    title: title,
    type: 'text',
    text: `${comic.pageStatus} / ${comic.pages}`,
  };
}

export function fromCategoryToRow(category: Category): Row {
  if (category === 'marvel') {
    return {
      title: 'Publisher',
      type: 'image',
      src: 'assets/marvel-240.png',
      alt: 'Marvel Comics',
    };
  }
  if (category === 'dc') {
    return {
      title: 'Publisher',
      type: 'image',
      src: 'assets/dc-240.png',
      alt: 'DC Comics',
    };
  }
  return {
    title: 'Publisher',
    type: 'text',
    text: 'Other',
  };
}

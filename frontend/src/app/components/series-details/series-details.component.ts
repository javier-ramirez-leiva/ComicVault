import { Component, OnInit, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import {
  ComicsService,
  MultiSelectService,
  ConfigURLService,
  WindowService,
  GridService,
  NotifierService,
  ModalService,
} from 'services';
import { ComicsDatabase, Series } from 'interfaces';
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
import { ComicsDatabaseGridComponent } from '../comics-database-grid/comics-database-grid.component';
import { CommonModule } from '@angular/common';
import { DeleteSeriesButtonComponent } from '../delete-series-button/delete-series-button.component';
import { DownloadSeriesButtonComponent } from '../download-series-button/download-series-button.component';
import { HideRolesDirective } from 'directives';
import { Role } from 'interfaces';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';
import { SeriesNotFoundComponent } from '../series-not-found/series-not-found.component';
import { LoadingSpinnerPageComponent } from '../loading-spinner-page/loading-spinner-page.component';
import { Row, TwoColumnsTableComponent } from '../two-columns-table/two-columns-table.component';
import { fromCategoryToRow } from '../comic-database-details/comic-database-details.component';
import { ComicDatabaseTableComponent } from '../comic-database-table/comic-database-table.component';
import { EditMetadataButtonComponent } from '../edit-metadata-button/edit-metadata-button.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SeenButtonComponent } from '../seen-button/seen-button.component';
import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';
import { ModalMessageComponent } from '../modal-message/modal-message.component';
import { CanComponentDeactivate } from 'src/app/guard/unsaved-changes-guard.guard';

@Component({
  selector: 'app-series-details',
  imports: [
    ComicsDatabaseGridComponent,
    RouterModule,
    CommonModule,
    DeleteSeriesButtonComponent,
    SeenButtonComponent,
    DownloadSeriesButtonComponent,
    HideRolesDirective,
    SeriesNotFoundComponent,
    LoadingSpinnerPageComponent,
    TwoColumnsTableComponent,
    ComicDatabaseTableComponent,
    EditMetadataButtonComponent,
  ],
  templateUrl: './series-details.component.html',
})
@UntilDestroy()
export class SeriesDetailsComponent implements OnInit, CanComponentDeactivate {
  private readonly route = inject(ActivatedRoute);
  private readonly comicsService = inject(ComicsService);
  private readonly configURLService = inject(ConfigURLService);
  private readonly multiSelectService = inject(MultiSelectService);
  private readonly windowService = inject(WindowService);
  protected readonly gridService = inject(GridService);
  private readonly notifierService = inject(NotifierService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly modalService = inject(ModalService);
  protected readonly series$: Observable<Series | undefined>;
  protected readonly comics$!: Observable<ComicsDatabase[]>;
  private series: Series | undefined = undefined;
  private comicsUpdate: ComicsDatabase[] = [];
  protected readonly notFoundID$ = new Subject<string>();
  private readonly reloadTrigger$ = new Subject<void>();
  protected readonly rows$: Observable<Row[]>;
  protected editStatus = false;
  protected validEdition = false;
  form: FormGroup;

  imageURL!: string;
  widthStyle: string = 'width: 0%';
  nextComicID: string | null = null;
  Role = Role;

  constructor() {
    this.form = this.formBuilder.group({
      table: this.formBuilder.group({
        title: new FormControl(''),
        year: new FormControl(''),
      }),
    });

    const id = String(this.route.snapshot.params['id']);

    this.series$ = combineLatest([
      this.multiSelectService.refresh$.pipe(startWith(null)),
      this.reloadTrigger$.pipe(startWith(null)),
    ])
      .pipe(switchMap(() => this.comicsService.getSeriesByID(id)))
      .pipe(
        catchError(() => {
          this.notFoundID$.next(id ?? '');
          return of(undefined);
        }),
        filter(notNullOrUndefined()),
        shareReplay({ bufferSize: 1, refCount: true }),
      );

    this.comics$ = this.series$.pipe(
      filter(notNullOrUndefined()),
      tap((series) => (this.series = series)),
      tap((series) =>
        this.form.patchValue({
          table: { title: series.title, year: series.year },
        }),
      ),
      map((series) => {
        this.imageURL = `${this.configURLService.baseURL}/${this.configURLService.apiVersion}/series/${series.id}/cover/medium`;
        for (let comic of series.comics) {
          if (!comic.readStatus) {
            comic.highlight = true;
            this.nextComicID = comic.id;
            break;
          }
        }
        const progress = 100 * (series.readIssues / series.totalIssues);
        this.widthStyle = `width: ${progress}%`;
        return series.comics;
      }),
      tap((comics) => (this.comicsUpdate = comics)),
      shareReplay({ bufferSize: 1, refCount: true }),
    );

    this.rows$ = this.series$.pipe(
      filter(notNullOrUndefined()),
      map((series) => [
        {
          title: 'Title',
          type: 'editable-text',
          formControlName: 'title',
          text: series.title,
        },
        fromCategoryToRow(series.category),
        {
          title: 'Year',
          type: 'editable-text',
          formControlName: 'year',
          text: series.year.trim().length > 0 ? series.year : 'N/A',
        },
        {
          title: 'Issues',
          type: 'text',
          text: series.readIssues + ' / ' + series.totalIssues,
        },
      ]),
    );
  }

  get table(): FormGroup {
    return this.form.get('table') as FormGroup;
  }

  canDeactivate(): boolean {
    if (this.editStatus) {
      return confirm('Are you sure you want to leave without saving?');
    }
    return true;
  }

  ngOnInit(): void {
    this.windowService.scrollToTop(0, 'smooth');
  }

  onEdit() {
    this.editStatus = true;
  }

  onComicsChange(comics: ComicsDatabase[]) {
    this.comicsUpdate = comics;
  }

  onSave() {
    this.editStatus = false;
    this.validEdition = true;

    if (this.series) {
      const comicIdsIssues: Map<string, number> = new Map(
        this.comicsUpdate.map((comic) => [comic.id, comic.issue]),
      );

      const value = this.form.getRawValue();
      const properties = {
        year: value.table.year,
        title: value.table.title,
      };
      this.comicsService
        .setSeriesProperties(this.series.id, properties)
        .pipe(
          catchError((error) => {
            this.notifierService.appendNotification({
              id: 0,
              title: 'Error setting metadata: ',
              message: this.series?.title ?? '',
              type: 'error',
            });
            return EMPTY;
          }),
          switchMap(() => this.comicsService.setIssues(comicIdsIssues)),
          untilDestroyed(this),
        )
        .subscribe(() => {
          this.reloadTrigger$.next();
          this.notifierService.appendNotification({
            id: 0,
            title: 'Updated metadata: ',
            message: this.series?.title ?? '',
            type: 'success',
          });
          resetRouteCache();
        });
    }
  }

  toggleSeen(): void {
    if (this.series) {
      const message = `Are you sure you want to mark as ${this.series.readStatus ? 'not read' : 'read'} ${this.series?.title} with ${this.series?.comics.length} comics?`;
      const ids = this.series.comics.map((comic) => comic.id);
      this.modalService
        .open<boolean, { message: string | undefined }>(ModalMessageComponent, { message: message })
        .pipe(
          filter((response) => response === true),
          switchMap(() => this.comicsService.setComicListReadStatus(ids, !this.series?.readStatus)),
          untilDestroyed(this),
        )
        .pipe(untilDestroyed(this))
        .subscribe(() => {
          if (this.series) {
            this.series.readStatus = !this.series.readStatus;
          }
          this.notifierService.appendNotification({
            id: 0,
            title: `Series marked as ${this.series?.readStatus ? 'read' : 'not read'} `,
            message: this.series!.title,
            type: 'success',
          });
          resetRouteCache();
          this.reloadTrigger$.next();
        });
    }
  }

  onCancel() {
    this.editStatus = false;
    this.validEdition = false;
    this.reloadTrigger$.next();
  }
}

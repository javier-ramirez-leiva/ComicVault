import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Category } from 'interfaces';
import { Observable, filter, map, shareReplay, tap } from 'rxjs';
import { TrendingButtonComponent } from '../trending-button/trending-button.component';
import { CommonModule } from '@angular/common';
import { SearchInputComponent } from '../search-input/search-input.component';

@Component({
  selector: 'app-search-and-category',
  imports: [TrendingButtonComponent, CommonModule, SearchInputComponent],
  templateUrl: './search-and-category.component.html',
})
export class SearchAndCategoryComponent {
  private readonly router = inject(Router);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  protected readonly catergoryAndSearch$: Observable<CatergoryAndSearch>;
  protected readonly textModifier$: Observable<string>;

  query: string | null = null;
  tag: string | null = null;
  category: Category | null = null;

  readonly urlsToNotUpdate = ['/comics-search/', '/comics/'];

  constructor() {
    this.catergoryAndSearch$ = this.route.queryParams.pipe(
      filter(() => !this.urlsToNotUpdate.some((url) => this.router.url.startsWith(url))),
      map((params) => {
        if (this.router.url.startsWith('/search') && Object.keys(params).length > 0) {
          return {
            query: params['query'] ?? null,
            tag: params['tag'] ?? null,
            category: params['category'] ?? null,
          };
        } else {
          return {
            query: null,
            tag: null,
            category: null,
          };
        }
      }),
      shareReplay({ refCount: true, bufferSize: 1 }),
    );

    this.textModifier$ = this.catergoryAndSearch$.pipe(
      map(({ query, tag }) => {
        if (query) {
          return query;
        } else if (tag) {
          return tag;
        } else {
          return '';
        }
      }),
    );
  }

  onCategoryChange(category: Category) {
    this.category = category;
    this.query = null;
    this.tag = null;
    this.navigate();
  }

  onQueryChange(query: string) {
    this.query = query;
    this.category = null;
    this.tag = null;
    this.navigate();
  }

  private navigate() {
    let params = {};
    if (this.query) {
      params = { query: this.query };
    } else if (this.category) {
      params = { category: this.category };
    } else {
      params = { category: 'all' };
    }
    this.router.navigate(['/search'], { queryParams: params });
  }
}

type CatergoryAndSearch = {
  category: Category | null;
  query: string | null;
  tag: string | null;
};

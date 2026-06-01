import { Component, OnInit, inject } from '@angular/core';
import { SearchInputComponent } from '../search-input/search-input.component';
import { NavigationEnd, Router } from '@angular/router';
import { TopBarService } from 'services';
import { FilterFunnel, FunnelButtonComponent } from '../funnel-button/funnel-button.component';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

import { BackButtonComponent } from '../back-button/back-button.component';
import { AvatarDropdownComponent } from '../avatar-dropdown/avatar-dropdown.component';
import { SearchAndCategoryComponent } from '../search-and-category/search-and-category.component';
import { GridButtonComponent } from '../grid-button/grid-button.component';
import {
  AdvanceFilterFunnel,
  AdvanceFunnelButtonComponent,
} from '../advance-funnel-button/advance-funnel-button.component';
import { HideRolesDirective } from 'src/app/directives/hide-roles.directive';
import { Role } from 'src/app/interfaces/users';

@UntilDestroy()
@Component({
  selector: 'app-top-bar',
  imports: [
    SearchInputComponent,
    FunnelButtonComponent,
    BackButtonComponent,
    AvatarDropdownComponent,
    SearchAndCategoryComponent,
    GridButtonComponent,
    AdvanceFunnelButtonComponent,
    HideRolesDirective,
  ],
  templateUrl: './top-bar.component.html',
})
export class TopBarComponent implements OnInit {
  protected displaySettings: DisplaySettings = {
    displaySearchInputLibrary: false,
    displaySearchInputSearch: false,
    displayTitle: false,
    displayFunnel: false,
    displayAdvanceFunnel: false,
    displayTrending: false,
    displayGridButton: false,
  };

  private readonly router: Router = inject(Router);
  protected readonly topBarService: TopBarService = inject(TopBarService);

  protected readonly Role = Role;

  ngOnInit(): void {
    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.updateDisplaySettings(event.urlAfterRedirects);
      }
    });
  }

  emitSeachTextChangeLibEvent(text: string) {
    this.topBarService.emitTextChangeLibEvent(text);
  }

  emitFilterChange(filter: FilterFunnel) {
    this.topBarService.emitFilterChangeEvent(filter);
  }

  emitAdvanceFilterChange(filter: AdvanceFilterFunnel) {
    this.topBarService.emitAdvanceFilterChangeEvent(filter);
  }

  emitContentChange(content: boolean) {
    this.topBarService.emitContentChangeEvent(content);
  }

  private updateDisplaySettings(currentRoute: string): void {
    switch (true) {
      case currentRoute.startsWith('/home'):
        this.displaySettings = {
          displaySearchInputLibrary: false,
          displaySearchInputSearch: true,
          displayTitle: false,
          displayFunnel: false,
          displayAdvanceFunnel: false,
          displayTrending: false,
          displayGridButton: false,
        };
        break;

      case currentRoute.startsWith('/library'):
        this.displaySettings = {
          displaySearchInputLibrary: true,
          displaySearchInputSearch: false,
          displayTitle: false,
          displayFunnel: true,
          displayAdvanceFunnel: false,
          displayTrending: false,
          displayGridButton: true,
        };
        break;

      case currentRoute.startsWith('/advanceLibrary'):
        this.displaySettings = {
          displaySearchInputLibrary: true,
          displaySearchInputSearch: false,
          displayTitle: false,
          displayFunnel: false,
          displayAdvanceFunnel: true,
          displayTrending: false,
          displayGridButton: true,
        };
        break;

      case currentRoute.startsWith('/search'):
        this.displaySettings = {
          displaySearchInputLibrary: false,
          displaySearchInputSearch: true,
          displayTitle: false,
          displayFunnel: false,
          displayAdvanceFunnel: false,
          displayTrending: true,
          displayGridButton: true,
        };
        break;

      case currentRoute.startsWith('/comics/'):
      case currentRoute.startsWith('/comics-search'):
        this.displaySettings = {
          displaySearchInputLibrary: false,
          displaySearchInputSearch: true,
          displayTitle: false,
          displayFunnel: false,
          displayAdvanceFunnel: false,
          displayTrending: true,
          displayGridButton: false,
        };
        break;

      case currentRoute.startsWith('/series'):
        this.displaySettings = {
          displaySearchInputLibrary: false,
          displaySearchInputSearch: true,
          displayTitle: false,
          displayFunnel: false,
          displayAdvanceFunnel: false,
          displayTrending: true,
          displayGridButton: true,
        };
        break;

      case currentRoute.startsWith('/downloads'):
        this.displaySettings = {
          displaySearchInputLibrary: false,
          displaySearchInputSearch: true,
          displayTitle: false,
          displayFunnel: false,
          displayAdvanceFunnel: false,
          displayTrending: false,
          displayGridButton: true,
        };
        break;

      case currentRoute === '/user':
      case currentRoute.startsWith('/user?'):
      case currentRoute.startsWith('/user/'):
        this.displaySettings = {
          displaySearchInputLibrary: false,
          displaySearchInputSearch: true,
          displayTitle: false,
          displayFunnel: false,
          displayAdvanceFunnel: false,
          displayTrending: false,
          displayGridButton: false,
        };
        break;

      default:
        this.displaySettings = {
          displaySearchInputLibrary: false,
          displaySearchInputSearch: true,
          displayTitle: false,
          displayFunnel: false,
          displayAdvanceFunnel: false,
          displayTrending: false,
          displayGridButton: false,
        };
        break;
    }
  }
}

type DisplaySettings = {
  displaySearchInputLibrary: boolean;
  displaySearchInputSearch: boolean;
  displayTitle: boolean;
  displayFunnel: boolean;
  displayAdvanceFunnel: boolean;
  displayTrending: boolean;
  displayGridButton: boolean;
};

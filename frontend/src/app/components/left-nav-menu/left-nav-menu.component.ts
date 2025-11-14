import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { ActivePageService } from 'services';
import { HideRolesDirective, OutsideClickDirective } from 'directives';
import { Role } from 'interfaces';
import { ScanLibButtonComponent } from "../scan-lib-button/scan-lib-button.component";
import { CleanLibButtonComponent } from "../clean-lib-button/clean-lib-button.component";
import { DeleteLibButtonComponent } from "../delete-lib-button/delete-lib-button.component";
import { AuthService } from 'services';
import { DarkmodeService } from 'services';
import { RouterModule } from '@angular/router';
import { LeftNavButtonComponent } from "../left-nav-button/left-nav-button.component";
import { DarkModeButtonComponent } from "../dark-mode-button/dark-mode-button.component";


@Component({
  selector: 'app-left-nav-menu',
  imports: [CommonModule, HideRolesDirective, ScanLibButtonComponent, CleanLibButtonComponent, DeleteLibButtonComponent, RouterModule, LeftNavButtonComponent, DarkModeButtonComponent, OutsideClickDirective],
  templateUrl: './left-nav-menu.component.html'
})
export class LeftNavMenuComponent {

  protected readonly activePageService = inject(ActivePageService);
  protected readonly authService = inject(AuthService);
  protected readonly darkModeService = inject(DarkmodeService);
  protected readonly Role = Role;

  iconSrc: string = (this.darkModeService.darkModeSignal() === 'dark') ? 'assets/icon-192x192.png' : 'assets/icon-192x192.png'

  protected displayLibraryMiniMenu = false;
  private _open = false;

  @Input({ required: true })
  set open(value: boolean) {
    this._open = value;
    this.displayLibraryMiniMenu = this.displayLibraryMiniMenu && this._open;
  }

  get open(): boolean {
    return this._open;
  }

  setDisplayLibraryMenu(value: boolean) {
    this.displayLibraryMiniMenu = value;
  }

  toggleDisplayLibraryMenu() {
    this.displayLibraryMiniMenu = !this.displayLibraryMiniMenu;
  }
}

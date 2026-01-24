import { Component, inject } from '@angular/core';
import { AuthService } from 'services';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { OutsideClickDirective } from 'directives';
import { DarkmodeService } from 'services';
import { AvatarUserComponent } from '../avatar-user/avatar-user.component';

@Component({
  selector: 'app-avatar-dropdown',
  imports: [CommonModule, RouterModule, OutsideClickDirective, AvatarUserComponent],
  templateUrl: './avatar-dropdown.component.html',
})
export class AvatarDropdownComponent {
  protected authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly darkModeService = inject(DarkmodeService);

  protected displayyDropdown: boolean = false;

  toggleDropdown() {
    this.displayyDropdown = !this.displayyDropdown;
  }
  hideDropdown() {
    this.displayyDropdown = false;
  }

  ngOnInit() {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.displayyDropdown = false;
      }
    });
  }

  toggleDarkMode() {
    this.darkModeService.toggleDarkMode();
  }
}

import { Component, inject, OnInit } from '@angular/core';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { DarkmodeService } from 'services';

@UntilDestroy()
@Component({
  selector: 'app-offline-page',
  imports: [],
  templateUrl: './offline-page.component.html',
})
export class OfflinePageComponent implements OnInit {
  iconSrc: string = '';
  darkMode: DarkmodeService = inject(DarkmodeService);

  ngOnInit(): void {
    this.darkMode.isDarkMode$.pipe(untilDestroyed(this)).subscribe((dark) => {
      this.iconSrc = dark ? 'assets/icon-192x192.png' : 'assets/icon-light-192x192.png';
    });
  }
}

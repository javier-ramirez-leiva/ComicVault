import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { ComicsService } from 'services';
import { ComicsDatabase } from 'interfaces';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';

@UntilDestroy()
@Component({
  selector: 'app-seen-button',
  imports: [],
  templateUrl: './seen-button.component.html',
})
export class SeenButtonComponent {
  private readonly comicsService = inject(ComicsService);
  @Input({ required: true }) readStatus!: boolean;

  @Output() onClick = new EventEmitter<void>();

  click(event: Event) {
    this.onClick.emit();
  }
}

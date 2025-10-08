import { Component, EventEmitter, Input, OnInit, Output, Renderer2, inject } from '@angular/core';
import { Category } from 'interfaces';
import { TopBarService } from 'services';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { notNullOrUndefined } from 'src/app/utils/rsjx-operators';

@UntilDestroy()
@Component({
  selector: 'app-trending-button',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './trending-button.component.html'
})
export class TrendingButtonComponent implements OnInit {

  @Input({ required: true }) category!: Category | null;

  categoryControl = new FormControl(null);

  renderer = inject(Renderer2);
  topBarService = inject(TopBarService);

  @Output() categoryChange = new EventEmitter<Category>();

  ngOnInit(): void {
    this.categoryControl.valueChanges.pipe(
      filter(notNullOrUndefined()),
      untilDestroyed(this)
    )
      .subscribe((value: Category | null) => {
        if (value !== null) {
          this.categoryChange.emit(value);
        }
      });
  }

}
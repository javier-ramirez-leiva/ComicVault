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

  categoryControl = new FormControl<Category | null>(null);

  renderer = inject(Renderer2);
  topBarService = inject(TopBarService);

  @Output() categoryChange = new EventEmitter<Category>();

  _category!: Category | null;
  @Input({ required: true })
  set category(val: Category | null) {
    this._category = val;
    if (val === null) {
      this.categoryControl.setValue('all', { emitEvent: false });
    } else {
      this.categoryControl.setValue(val, { emitEvent: false });
    }

  }
  get category(): Category | null {
    return this._category;
  }

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
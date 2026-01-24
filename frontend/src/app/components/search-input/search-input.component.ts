import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TopBarService } from 'services';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { Observable } from 'rxjs';

@UntilDestroy()
@Component({
  selector: 'app-search-input',
  imports: [CommonModule, FormsModule],
  templateUrl: './search-input.component.html',
})
export class SearchInputComponent implements OnInit {
  @Input({ required: false }) searchButton: boolean = true;
  @Input({ required: false }) textModifier$!: Observable<string>;
  @Input({ required: false }) placeholder: string = 'Search';

  protected query: string = '';
  @Output() queryChange = new EventEmitter<string>();
  @Output() textChange = new EventEmitter<string>();

  ngOnInit(): void {
    if (this.textModifier$) {
      this.textModifier$.pipe(untilDestroyed(this)).subscribe((text) => {
        this.query = text;
      });
    }
  }

  onSearch() {
    this.queryChange.emit(this.query);
  }

  onTextChange(input: string) {
    this.textChange.emit(input);
  }
}

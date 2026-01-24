import { Component, EventEmitter, Input, Output, input } from '@angular/core';

@Component({
  selector: 'app-page-navigator',
  imports: [],
  templateUrl: './page-navigator.component.html',
})
export class PageNavigatorComponent {
  @Output() pageChange = new EventEmitter<number>();
  @Input() page: number = 1;

  previousPage() {
    if (this.page > 1) this.page--;
    this.pageChange.emit(this.page);
  }

  nextPage() {
    this.page++;
    this.pageChange.emit(this.page);
  }
}

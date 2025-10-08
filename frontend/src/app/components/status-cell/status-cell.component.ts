import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ComicsDatabase } from 'interfaces';

@Component({
  selector: 'app-status-cell',
  imports: [CommonModule],
  templateUrl: './status-cell.component.html',
})
export class StatusCellComponent {
  @Input({ required: true }) comic!: ComicsDatabase;

}

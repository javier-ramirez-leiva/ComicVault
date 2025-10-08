import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { History } from 'interfaces';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-history-table',
  imports: [CommonModule, RouterModule],
  templateUrl: './history-table.component.html',
})
export class HistoryTableComponent {
  @Input({ required: true }) histories: History[] = [];
}

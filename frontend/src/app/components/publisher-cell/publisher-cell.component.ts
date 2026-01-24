import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { Category } from 'interfaces';

@Component({
  selector: 'app-publisher-cell',
  imports: [CommonModule],
  templateUrl: './publisher-cell.component.html',
})
export class PublisherCellComponent {
  @Input({ required: true }) category!: Category;
}

import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { GridService } from 'services';

@Component({
  selector: 'app-grid-button',
  imports: [CommonModule],
  templateUrl: './grid-button.component.html',
})
export class GridButtonComponent {
  protected readonly gridService = inject(GridService);

  toggleGrid() {
    this.gridService.toggleGrid();
  }
}

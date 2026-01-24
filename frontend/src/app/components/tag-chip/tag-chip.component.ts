import { Component, Input, inject } from '@angular/core';
import { Tag } from 'interfaces';
import { Router } from '@angular/router';
import { resetRouteCache } from 'src/app/strategy_providers/custom-reuse-strategy';

@Component({
  selector: 'app-tag-chip',
  imports: [],
  templateUrl: './tag-chip.component.html',
})
export class TagChipComponent {
  @Input({ required: true }) tag!: Tag;

  private readonly router = inject(Router);

  onClick() {
    resetRouteCache();
    this.router.navigate(['/search'], { queryParams: { tag: this.tag.link } });
  }
}

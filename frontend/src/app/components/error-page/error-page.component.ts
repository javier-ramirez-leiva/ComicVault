import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-page',
  imports: [CommonModule],
  templateUrl: './error-page.component.html',
})
export class ErrorPageComponent {
  @Input({ required: false }) message!: string;
}

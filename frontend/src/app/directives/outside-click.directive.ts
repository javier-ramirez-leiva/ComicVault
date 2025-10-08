import { Directive, ElementRef, EventEmitter, Output, OnInit, OnDestroy } from '@angular/core';

@Directive({
  selector: '[appOutsideClick]',
  standalone: true
})
export class OutsideClickDirective implements OnInit, OnDestroy {
  @Output() outsideClick = new EventEmitter<void>();

  private clickListener!: (event: Event) => void;

  constructor(private elementRef: ElementRef) { }

  ngOnInit() {
    this.clickListener = (event: Event) => {
      const clickedInside = this.elementRef.nativeElement.contains(event.target);
      if (!clickedInside) {
        this.outsideClick.emit();
      }
    };

    // listen in capture phase => true
    document.addEventListener('click', this.clickListener, true);
  }

  ngOnDestroy() {
    document.removeEventListener('click', this.clickListener, true);
  }
}
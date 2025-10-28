import {
    Directive,
    ElementRef,
    EventEmitter,
    Output,
    OnDestroy,
    OnInit,
    Renderer2,
    NgZone
} from '@angular/core';

@Directive({
    selector: '[appOutsideMenuClick]',
    standalone: true
})
export class OutsideMenuClickDirective implements OnInit, OnDestroy {
    @Output() outsideClick = new EventEmitter<void>();
    private removeClickListener?: () => void;

    constructor(
        private elementRef: ElementRef<HTMLElement>,
        private renderer: Renderer2,
        private ngZone: NgZone
    ) { }

    ngOnInit() {
        this.ngZone.runOutsideAngular(() => {
            this.removeClickListener = this.renderer.listen('document', 'mousedown', (event: MouseEvent) => {
                const menuElement = this.elementRef.nativeElement;
                const path = event.composedPath ? event.composedPath() : [];

                // Check if the click happened inside the menu element
                const clickedInside =
                    path.includes(menuElement) ||
                    menuElement.contains(event.target as Node);

                if (!clickedInside) {
                    this.ngZone.run(() => this.outsideClick.emit());
                }
            });
        });
    }

    ngOnDestroy() {
        this.removeClickListener?.();
    }
}
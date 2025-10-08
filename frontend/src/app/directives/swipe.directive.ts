import { Directive, EventEmitter, HostListener, Output, inject } from '@angular/core';
import { WindowService } from '../services/window.service';

@Directive({ selector: '[swipe]' })
export class SwipeDirective {
    @Output() next = new EventEmitter<void>();
    @Output() previous = new EventEmitter<void>();

    private readonly windowService = inject(WindowService);

    swipeCoord = [0, 0];
    swipeTime = new Date().getTime();

    constructor() { }

    @HostListener('touchstart', ['$event']) onSwipeStart($event: TouchEvent) {
        this.onSwipe($event, 'start');
    }

    @HostListener('touchend', ['$event']) onSwipeEnd($event: TouchEvent) {
        this.onSwipe($event, 'end');
    }

    onSwipe(e: TouchEvent, when: string) {
        this.swipe(e, when);
    }

    swipe(e: TouchEvent, when: string): void {

        const coord: [number, number] = [e.changedTouches[0].clientX, e.changedTouches[0].clientY];
        const time = new Date().getTime();

        if (when === 'start') {
            this.swipeCoord = coord;
            this.swipeTime = time;
        } else if (when === 'end') {
            const direction = [coord[0] - this.swipeCoord[0], coord[1] - this.swipeCoord[1]];
            const duration = time - this.swipeTime;

            if (duration < 1000 //
                && !this.windowService.isZoomed()//
                && Math.abs(direction[0]) > 30 // Long enough
                && Math.abs(direction[0]) > Math.abs(direction[1] * 3)) { // Horizontal enough
                const swipeDir = direction[0] < 0 ? 'next' : 'previous';
                if (swipeDir === 'next') {
                    this.next.emit();
                } else {
                    this.previous.emit();
                }
            }
        }
    }
}
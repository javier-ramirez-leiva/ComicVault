import { Directive, ElementRef, Input, OnInit, Renderer2, inject } from '@angular/core';
import { AuthService } from 'services';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { Role } from 'interfaces';

@Directive({
  selector: '[appHideRoles]',
})
@UntilDestroy()
export class HideRolesDirective implements OnInit {
  @Input() appHideForRoles: Role[] = [];
  renderer = inject(Renderer2);
  elementRef = inject(ElementRef);
  authService = inject(AuthService);

  constructor() {}

  ngOnInit() {
    this.authService.role$.pipe(untilDestroyed(this)).subscribe((role) => {
      if (this.appHideForRoles.includes(role)) {
        this.renderer.setStyle(this.elementRef.nativeElement, 'display', 'none');
      } else {
        this.renderer.removeStyle(this.elementRef.nativeElement, 'display');
      }
    });
  }
}

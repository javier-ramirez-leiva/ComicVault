import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ModalService } from 'services';
import { Router } from '@angular/router';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 'app-modal-container',
  imports: [CommonModule],
  templateUrl: './modal-container.component.html',
})
export class ModalContainerComponent implements OnInit, AfterViewInit {
  protected readonly modalService = inject(ModalService);
  private readonly router = inject(Router);

  protected display: boolean = false;

  ngOnInit(): void {
    this.router.events.pipe(untilDestroyed(this)).subscribe((event) => {
      this.closeModal();
    });
  }

  ngAfterViewInit() {
    if (typeof document !== 'undefined') {
      const container = document.getElementById('modal-content');
      if (container) {
        this.modalService.setModalContainer(container);
      } else {
        console.error('Modal container element not found!');
      }
    }
  }

  closeModal() {
    if (this.modalService.isActive()) {
      this.modalService.close();
    }
  }
}

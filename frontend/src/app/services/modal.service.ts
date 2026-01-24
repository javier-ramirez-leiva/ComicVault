import {
  Injectable,
  ComponentRef,
  Injector,
  ApplicationRef,
  ComponentFactoryResolver,
  Type,
} from '@angular/core';
import { Subject, Observable, BehaviorSubject } from 'rxjs';
import { ModalComponent } from 'interfaces';

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  private modalContainer!: HTMLElement;
  private activeModalRef?: ComponentRef<any>;
  private resultSubject?: Subject<any>;
  private activeModal = false;

  public activeModal$ = new BehaviorSubject<boolean>(false);
  public animationModal$ = new BehaviorSubject<boolean>(false);

  constructor(
    private resolver: ComponentFactoryResolver,
    private appRef: ApplicationRef,
    private injector: Injector,
  ) {}

  setModalContainer(container: HTMLElement) {
    this.modalContainer = container;
  }

  open<T, D>(component: Type<ModalComponent<T, D>>, data?: D): Observable<T> {
    if (!this.modalContainer) {
      throw new Error('Modal container is not set. Ensure to call setModalContainer().');
    }

    this.close(); // Close any existing modal

    const factory = this.resolver.resolveComponentFactory(component);
    this.activeModalRef = factory.create(this.injector);

    if (data) {
      this.activeModalRef.instance.data = data; // Assign input data
    }

    this.resultSubject = new Subject<T>();

    this.activeModalRef.instance.close = (response: T) => {
      this.resultSubject?.next(response);
      this.resultSubject?.complete();
      this.close();
    };

    this.appRef.attachView(this.activeModalRef.hostView);
    this.modalContainer.appendChild(this.activeModalRef.location.nativeElement);

    this.activeModal$.next(true);
    this.activeModal = true;

    return this.resultSubject.asObservable();
  }

  close() {
    if (this.activeModalRef) {
      this.appRef.detachView(this.activeModalRef.hostView);
      this.activeModalRef.destroy();
      this.activeModalRef = undefined;
      this.activeModal$.next(false);
      this.activeModal = false;
    }
  }

  isActive(): boolean {
    return this.activeModal;
  }

  loadingAnimation(loading: boolean) {
    this.activeModal$.next(loading);
    this.animationModal$.next(loading);
  }
}

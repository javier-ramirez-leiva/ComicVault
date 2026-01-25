export interface ModalComponent<T = any, D = any> {
  close: (response?: T) => void;
  data?: D;
}

import { distinctUntilChanged } from 'rxjs/operators';
import isEqual from 'lodash/isEqual';

export function notNullOrUndefined<T>() {
  return (value: T | null | undefined): value is T => value !== null && value !== undefined;
}

export const distinctUntilValueChanged = <T>() => distinctUntilChanged<T>((a, b) => isEqual(a, b));

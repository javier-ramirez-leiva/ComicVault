import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'emptyToNA'
})
export class EmptyToNAPipe implements PipeTransform {

  transform(value: string | null | undefined): string {
    if (value === null || value === undefined || value.trim() === '') {
      return 'N/A';
    }
    return value;
  }

}

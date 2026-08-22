import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'duration',
  standalone: true,
})
export class DurationPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    const parts = value.split(':').map(Number);

    if (parts.length !== 3 || parts.some(isNaN)) {
      return value;
    }

    const [hours, minutes, seconds] = parts;

    const result: string[] = [];

    if (hours > 0) {
      result.push(`${hours}h`);
    }

    if (minutes > 0) {
      result.push(`${minutes}m`);
    }

    if (seconds > 0 || result.length === 0) {
      result.push(`${seconds}s`);
    }

    return result.join(' ');
  }
}

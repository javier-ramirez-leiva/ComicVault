export function formatDate(timestamp?: string | number | Date): string {
    if (!timestamp) return '';

    const date = new Date(timestamp);

    return new Intl.DateTimeFormat('en-US', {
        month: 'short', // "MMM"
        day: 'numeric', // "d"
        year: 'numeric', // "y"
        hour: 'numeric', // "h"
        minute: '2-digit', // "mm"
        hour12: true, // "a"
    }).format(date);
}
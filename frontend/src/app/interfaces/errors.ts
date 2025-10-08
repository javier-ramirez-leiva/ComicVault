const ERROR_CODES = [
    'RESOURCE_NOT_FOUND',
    'INVALID_ARGUMENT',
    'BAD_AUTH_REQUEST',
    'SCRAPER_GATEWAY_ERROR',
    'SCRAPER_PAGE_GATEWAY_ERROR',
    'SCRAPER_PARSING_ERROR',
    'INVALID_CONFIGURATION_ARGUMENT',
    'SLACK_GATEWAY_ERROR',
    'SCRAPER_UNKNOWN_ERROR',
    'RESOURCE_ENTITY_NOT_FOUND',
    'RESOURCE_ALREADY_EXISTING_FOUND',
    'ONE_ADMIN',
    'EMPTY_SERIES',
    'AUTH_ERROR',
    'RESOURCE_FILE_NOT_FOUND',
    'USER_DETAILS_NOT_FOUND',
    'INTERNAL_ERROR',
];

export type HttpResponseErrorCode = (typeof ERROR_CODES)[number];

export interface HttpResponseError {
    message: string;
    errorCode: HttpResponseErrorCode;
    status: number;
}

export function isHttpResponseErrorCode(code: string): code is HttpResponseErrorCode {
    return ERROR_CODES.includes(code as HttpResponseErrorCode);
}

export function isHttpResponseError(obj: any): obj is HttpResponseError {
    return (
        typeof obj === 'object' &&
        obj !== null &&
        typeof obj.status === 'number' &&
        typeof obj.message === 'string' &&
        isHttpResponseErrorCode(obj.errorCode)
    );
}




export type DownloadStatus = "downloaded" | "downloading" | "not-downloaded";

export interface Tag {
    link: string;
    name: string;
}

export type DeleteReadOptions =
    `READ_BY_ME` |
    `READ_BY_ALL` |
    `READ_BY_ME_NOT_STARTED_BY_OTHER`
    ;

export interface ComicsSearch {
    link: string;
    category: Category;
    idGc: string;
    image: string;
    size: string;
    title: string;
    year: string;
    downloadingStatus: DownloadStatus;
    totalBytes: number,
    currentBytes: number,
    totalComics: number,
    currentComic: number,
    highlight: boolean;
}

export interface ComicSearchDetails extends ComicsSearch {
    description: string;
    tags: Tag[];
    mainTag: Tag | null;
}

export interface ComicSearchDetailsLinks extends ComicSearchDetails {
    downloadIssues: DownloadIssue[];
}

export interface ComicsDatabase {
    id: string;
    idGc: string;
    idGcIssue: string;
    description: string;
    createdAt: string;
    path: string;
    title: string;
    issue: number;
    category: Category;
    year: string;
    size: string;
    pages: number;
    pageStatus: number;
    readStatus: boolean;
    link: string;
    seriesID: string;
    seriesTitle: string;
    tags: Tag[];
    doublePages: number[];
    doublePageCover: boolean;

    //UI stuff
    highlight: boolean;
}

export interface DownloadLink {
    link: string;
    platform: string;
}
export interface DownloadIssue {
    description: string;
    idGcIssue: string;
    links: DownloadLink[];
    title: string;
}

export interface DownloadIssueRequest {
    description: string;
    idGcIssue: string;
    link: DownloadLink | undefined;
    title: string;
}

export interface DownloadRequest {
    comicSearchDetails: ComicSearchDetails;
    downloadLink: DownloadLink;
}

export interface DownloadRequestList {
    comicSearchDetails: ComicSearchDetails;
    downloadRequests: DownloadIssueRequest[];
}

export interface Series {
    id: string;
    title: string;
    year: string;
    readStatus: boolean;
    category: Category;
    readIssues: number;
    totalIssues: number;
    comics: ComicsDatabase[];
}

export interface ScrapperResponse {
    comicsSearchs: ComicsSearch[];
    endReached: boolean;
}

export type Category = 'all' | 'marvel' | 'dc' | 'other-comics';
export function isCategory(value: string): value is Category {
    return ['all', 'marvel', 'dc', 'other-comics'].includes(value);
}
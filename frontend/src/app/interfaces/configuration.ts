export interface Configuration {
    apiVersion: string;
    baseURL: string;
}

export interface SlackConfiguration {
    slackWebHook: string;
    comicVaultBaseUrl: string;
    enableNotifications: boolean;
}

export interface JDownloaderConfiguration {
    jdownloaderOutputPath: string;
    jdownloaderCrawljobPath: string;
    deleteFolderOutputFolder: boolean;
}


export interface ComicsConfiguration {
    downloadRoot: string;
    scanArchives: boolean;
    deleteArchives: boolean;
    generateNavigationThumbnails: boolean;
    slackConfiguration: SlackConfiguration;
    comicVine_apiKey: string;
    jdownloaderConfiguration: JDownloaderConfiguration;
    getComicsBaseUrl: string;
}
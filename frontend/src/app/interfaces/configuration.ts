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

export interface GetComicsConfiguration {
  baseUrl: string;
  requestsPerSecond: number;
}

export interface ComicsConfiguration {
  downloadRoot: string;
  scanArchives: boolean;
  deleteArchives: boolean;
  generateNavigationThumbnails: boolean;
  autoMetadata: boolean;
  slackConfiguration: SlackConfiguration;
  comicVineAPIKey: string;
  jdownloaderConfiguration: JDownloaderConfiguration;
  getComicsConfiguration: GetComicsConfiguration;
}

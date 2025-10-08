package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.ConfigurationDTO;
import org.comicVaultBackend.domain.dto.JDownloaderConfigurationDTO;
import org.comicVaultBackend.domain.dto.SlackConfigurationDTO;
import org.comicVaultBackend.exceptions.*;

import java.nio.file.Path;

public interface ConfigurationService {

    Path getResourcesPath();

    ConfigurationDTO getConfiguration();

    Path getNotificationFile();

    void setDownloadRoot(String downloadRoot) throws ConfigurationArgumentException;

    void setSlackConfiguration(SlackConfigurationDTO slackConfiguration) throws SlackNotifyException, ConfigurationArgumentException;

    void setGetComicsBaseUrl(String getComicsBaseUrl) throws ConfigurationArgumentException, ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException;

    void setSlackNotify(Boolean slackNotify) throws ConfigurationArgumentException;

    void setComicVineAPIKey(String comicVineAPIKey) throws ConfigurationArgumentException;

    void setJDownloaderConfiguration(JDownloaderConfigurationDTO jDownloaderConfiguration) throws ConfigurationArgumentException;

    void setScanArchives(Boolean scanArchives) throws ConfigurationArgumentException;

    void setDeleteArchives(Boolean deleteArchives) throws ConfigurationArgumentException;

    void setGenerateNavigationThumbnails(Boolean generateNavigationThumbnails) throws ConfigurationArgumentException;
}

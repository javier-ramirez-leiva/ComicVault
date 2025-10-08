package org.comicVaultBackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.comicVaultBackend.domain.dto.ConfigurationDTO;
import org.comicVaultBackend.domain.dto.JDownloaderConfigurationDTO;
import org.comicVaultBackend.domain.dto.SlackConfigurationDTO;
import org.comicVaultBackend.exceptions.*;
import org.comicVaultBackend.services.ConfigurationService;
import org.comicVaultBackend.services.GetComicsScrapperService;
import org.comicVaultBackend.services.SlackNotifyService;
import org.comicVaultBackend.services.URLBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private ConfigurationDTO configurationDto;
    private Path configurationFilePath;
    private Path notificationFilePath;
    private Path resourcesFilePath;
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private URLBuilderService urlBuilderService;
    @Autowired
    private GetComicsScrapperService getComicsScrapperService;

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);


    @PostConstruct
    public void init() throws ConfigurationArgumentException {
        String backendEnv = System.getenv("BACKEND_ENV");
        if (backendEnv != null && backendEnv.equals("production")) {
            resourcesFilePath = Paths.get("/data");
            configurationFilePath = Paths.get("/data", "config.json");
        } else {
            try {
                Resource resource = resourceLoader.getResource("classpath:application.properties");
                resourcesFilePath = Paths.get(resource.getURI()).toAbsolutePath().getParent();
                configurationFilePath = Paths.get(resourcesFilePath.toString(), "config.json");

            } catch (IOException e) {
                logger.error("Error getting application.properties path file: {}", e.getMessage());
                throw new ConfigurationArgumentException("Error getting application.properties path file: " + e.getMessage(), e);
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        if (Files.exists(configurationFilePath)) {
            try {
                configurationDto = objectMapper.readValue(new File(configurationFilePath.toString()), ConfigurationDTO.class);
            } catch (IOException e) {
                logger.error("Error reading configuration file: {}: {}", configurationFilePath, e.getMessage());
                throw new ConfigurationArgumentException("Error reading configuration file: " + configurationFilePath + ": " + e.getMessage(), e);
            }
        } else {
            final String getComicsBaseUrl = "https://getcomics.org"; // Working as of today 23/04/2025
            this.configurationDto = ConfigurationDTO.builder().downloadRoot("").comicVineAPIKey("").getComicsBaseUrl(getComicsBaseUrl)
                    .slackConfiguration(SlackConfigurationDTO.builder().enableNotifications(false).slackWebHook("").comicVaultBaseUrl("").build())
                    .jDownloaderConfiguration(JDownloaderConfigurationDTO.builder().jDownloaderOutputPath("").jDownloaderCrawljobPath("").build()).
                    deleteArchives(true).scanArchives(true).generateNavigationThumbnails(true).build();
            updateConfigurationFile();
        }
        urlBuilderService.setBaseURL(this.configurationDto.getGetComicsBaseUrl());
        getComicsScrapperService.setBaseURL(this.configurationDto.getGetComicsBaseUrl());
        notificationFilePath = Paths.get(resourcesFilePath.toString(), "notify_slack.json");
        logger.info("Configuration file found: {}", configurationFilePath);
        logger.info("Notification file found: {}", notificationFilePath);
    }

    @Override
    public Path getResourcesPath() {
        return resourcesFilePath;
    }

    @Override
    public ConfigurationDTO getConfiguration() {
        return configurationDto;
    }


    @Override
    public Path getNotificationFile() {
        return notificationFilePath;
    }


    @Override
    public void setDownloadRoot(String downloadRoot) throws ConfigurationArgumentException {
        Path downloadRootPath = Paths.get(downloadRoot);
        if (!Files.isDirectory(downloadRootPath)) {
            String message = String.format("'downloadRoot' path is not a folder: %s", downloadRoot);
            logger.error(message);
            throw new ConfigurationArgumentException(message);
        }
        if (!Files.isWritable(downloadRootPath)) {
            String message = String.format("'downloadRoot' path is not writable: %s", downloadRoot);
            logger.error(message);
            throw new ConfigurationArgumentException(message);
        }
        configurationDto.setDownloadRoot(downloadRoot);
        updateConfigurationFile();
        logger.info("Configuration value `downloadRoot` value updated: {}", downloadRoot);
    }

    @Override
    public void setSlackConfiguration(SlackConfigurationDTO slackConfiguration) throws SlackNotifyException, ConfigurationArgumentException {
        SlackNotifyService slackNotifyService = new SlackNotifyServiceImpl();
        try {
            slackNotifyService.sendNotification("Test", getNotificationFile(), slackConfiguration.getSlackWebHook(), slackConfiguration.getComicVaultBaseUrl(), null, null);
        } catch (SlackNotifyException ex) {
            logger.error("Invalid slackConfiguration: {}", slackConfiguration);
            throw ex;
        }
        configurationDto.setSlackConfiguration(slackConfiguration);
        updateConfigurationFile();
        logger.info("Configuration value `slackConfiguration` updated: {}", slackConfiguration);
    }

    @Override
    public void setGetComicsBaseUrl(String getComicsBaseUrl) throws ConfigurationArgumentException, ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {
        urlBuilderService.setBaseURL(getComicsBaseUrl);
        String url = urlBuilderService.baseURL(1);
        try {
            getComicsScrapperService.getComics(url, 1);
        } catch (ComicScrapperParsingException | ComicScrapperGatewayException | ComicScrapperGatewayPageException ex) {
            logger.error("Invalid getComicsBaseUrl: {}", getComicsBaseUrl);
            throw ex;
        }
        getComicsScrapperService.setBaseURL(getComicsBaseUrl);
        configurationDto.setGetComicsBaseUrl(getComicsBaseUrl);
        updateConfigurationFile();
        logger.info("Configuration value `getComicsBaseUrl` updated: {}", getComicsBaseUrl);
    }

    @Override
    public void setSlackNotify(Boolean slackNotify) throws ConfigurationArgumentException {
        configurationDto.getSlackConfiguration().setEnableNotifications(slackNotify);
        updateConfigurationFile();
        logger.info("Configuration value `slackNotify` updated: {}", slackNotify);
    }


    @Override
    public void setComicVineAPIKey(String comicVineAPIKey) throws ConfigurationArgumentException {
        configurationDto.setComicVineAPIKey(comicVineAPIKey);
        updateConfigurationFile();
        logger.info("Configuration value `comicVineAPIKey` updated: {}", comicVineAPIKey);
    }

    @Override
    public void setJDownloaderConfiguration(JDownloaderConfigurationDTO jDownloaderConfiguration) throws ConfigurationArgumentException {
        Path jDownloaderCrawljobPath = Paths.get(jDownloaderConfiguration.getJDownloaderCrawljobPath());
        if (!Files.isDirectory(jDownloaderCrawljobPath)) {
            String message = String.format("'jDownloader crawljob' path is not a folder: %s", jDownloaderCrawljobPath);
            logger.error(message);
            throw new ConfigurationArgumentException(message);
        }
        if (!Files.isWritable(jDownloaderCrawljobPath)) {
            String message = String.format("'jDownloader crawljob' path is not writable: %s", jDownloaderCrawljobPath);
            logger.error(message);
            throw new ConfigurationArgumentException(message);
        }
        configurationDto.setJDownloaderConfiguration(jDownloaderConfiguration);
        updateConfigurationFile();
        logger.info("Configuration value `jDownloaderConfiguration` value updated: {}", jDownloaderConfiguration);
    }


    @Override
    public void setScanArchives(Boolean scanArchives) throws ConfigurationArgumentException {
        configurationDto.setScanArchives(scanArchives);
        updateConfigurationFile();
        logger.info("Configuration value `scanArchives` updated: {}", scanArchives);
    }

    @Override
    public void setDeleteArchives(Boolean deleteArchives) throws ConfigurationArgumentException {
        configurationDto.setDeleteArchives(deleteArchives);
        updateConfigurationFile();
        logger.info("Configuration value `deleteArchives` updated: {}", deleteArchives);
    }

    @Override
    public void setGenerateNavigationThumbnails(Boolean generateNavigationThumbnails) throws ConfigurationArgumentException {
        configurationDto.setGenerateNavigationThumbnails(generateNavigationThumbnails);
        updateConfigurationFile();
        logger.info("Configuration value `generateNavigationThumbnails` updated: {}", generateNavigationThumbnails);
    }

    private void updateConfigurationFile() throws ConfigurationArgumentException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(configurationFilePath.toString()), configurationDto);

        } catch (IOException e) {
            throw new ConfigurationArgumentException("Error writing configuration file: " + configurationFilePath + ": " + e.getMessage(), e);
        }
    }
}

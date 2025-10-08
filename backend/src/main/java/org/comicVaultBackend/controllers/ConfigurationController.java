package org.comicVaultBackend.controllers;

import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.ConfigurationDTO;
import org.comicVaultBackend.domain.dto.JDownloaderConfigurationDTO;
import org.comicVaultBackend.domain.dto.SlackConfigurationDTO;
import org.comicVaultBackend.exceptions.*;
import org.comicVaultBackend.services.ConfigurationService;
import org.comicVaultBackend.services.GetComicsScrapperService;
import org.comicVaultBackend.services.URLBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.version}/configuration")
public class ConfigurationController {
    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private GetComicsScrapperService getComicsScrapperService;

    @Autowired
    private URLBuilderService urlBuilderService;


    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping()
    public ConfigurationDTO configuration() {
        return configurationService.getConfiguration();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/downloadRoot")
    public void setDownloadRoot(@RequestBody String downloadRoot) throws ConfigurationArgumentException {
        configurationService.setDownloadRoot(downloadRoot);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/slack")
    public void setSlackWebHook(@RequestBody SlackConfigurationDTO slackConfiguration) throws ConfigurationArgumentException, SlackNotifyException {
        configurationService.setSlackConfiguration(slackConfiguration);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/getComicsBaseUrl")
    public void setGetComicsBaseUrl(@RequestBody String getComicsBaseUrl) throws ComicScrapperParsingException, ConfigurationArgumentException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {
        configurationService.setGetComicsBaseUrl(getComicsBaseUrl);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/slackNotify")
    public void setSlackNotify(@RequestBody Boolean slackNotify) throws ConfigurationArgumentException {
        configurationService.setSlackNotify(slackNotify);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/comicVine_apiKey")
    public void setComicVine_apiKey(@RequestBody String comicVine_apiKey) throws ConfigurationArgumentException {
        configurationService.setComicVineAPIKey(comicVine_apiKey);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/jDownloader")
    public void setjDownloaderConfiguration(@RequestBody JDownloaderConfigurationDTO jDownloaderConfiguration) throws ConfigurationArgumentException {
        configurationService.setJDownloaderConfiguration(jDownloaderConfiguration);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/scanArchives")
    public void setScanArchives(@RequestBody Boolean scanArchives) throws ConfigurationArgumentException {
        configurationService.setScanArchives(scanArchives);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/deleteArchives")
    public void setDeleteArchives(@RequestBody Boolean deleteArchives) throws ConfigurationArgumentException {
        configurationService.setDeleteArchives(deleteArchives);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/generateNavigationThumbnails")
    public void setGenerateNavigationThumbnails(@RequestBody Boolean generateThumbnails) throws ConfigurationArgumentException {
        configurationService.setGenerateNavigationThumbnails(generateThumbnails);
    }
}

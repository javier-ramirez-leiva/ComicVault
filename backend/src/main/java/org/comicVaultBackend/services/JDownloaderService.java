package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.ComicSearchDTO;
import org.comicVaultBackend.domain.dto.DownloadLinkDTO;
import org.comicVaultBackend.domain.dto.JDownloaderConfigurationDTO;
import org.comicVaultBackend.exceptions.DownloadException;

import java.nio.file.Path;

public interface JDownloaderService {
    public Path downloadFile(Path downloadRoot, Path parentFolder, DownloadLinkDTO downloadLink, String title, ComicSearchDTO comicSearchDto, String description, JDownloaderConfigurationDTO jDownloaderConfiguration) throws DownloadException;
}

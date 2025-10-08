package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.ComicSearchDTO;
import org.comicVaultBackend.domain.dto.DownloadLinkDTO;
import org.comicVaultBackend.domain.dto.JDownloaderConfigurationDTO;
import org.comicVaultBackend.exceptions.DownloadException;

import java.nio.file.Path;
import java.util.List;

public interface DownloadService {

    Path downloadFile(Path downloadRoot, Path parentFolder, DownloadLinkDTO downloadLinkDto, String title, ComicSearchDTO comicSearchDto, String description, JDownloaderConfigurationDTO jDownloaderConfiguration) throws DownloadException;

    List<Path> extractComics(Path zipFolder) throws DownloadException;

    List<ComicSearchDTO> getListDownloadingComics();

    //Call must be explicit since this service does not manage adding comic to DB
    void removeComicFromDownloadList(ComicSearchDTO comicSearchDTO);
}

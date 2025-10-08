package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.ComicDTO;
import org.comicVaultBackend.domain.dto.ComicSearchDetailsDTO;
import org.comicVaultBackend.domain.dto.SeriesDTO;
import org.comicVaultBackend.exceptions.ComicFileException;
import org.comicVaultBackend.exceptions.ComicMetaDataException;

import java.nio.file.Path;

public interface ComicMetaDataService {
    ComicDTO getComicMetaData(Path filePath, ComicSearchDetailsDTO comicSearchDetails, String baseUrl) throws ComicMetaDataException, ComicFileException;

    String getComicID(Path filePath) throws ComicMetaDataException;

    SeriesDTO getSeriesMetaData(ComicDTO comicDto) throws ComicMetaDataException;

    ComicSearchDetailsDTO getMetadataFromComicPath(Path file) throws ComicMetaDataException;
}

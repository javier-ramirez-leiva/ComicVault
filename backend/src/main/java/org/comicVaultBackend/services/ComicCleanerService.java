package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.domain.entities.JobEntity;
import org.comicVaultBackend.domain.entities.SeriesEntity;
import org.comicVaultBackend.exceptions.FileManagerException;

public interface ComicCleanerService {
    String deleteComic(ComicEntity comicEntity, boolean deleteFiles, boolean deleteSeries, JobEntity jobEntity) throws FileManagerException;

    void deleteSeries(SeriesEntity seriesEntity, boolean deleteFiles, JobEntity jobEntity) throws FileManagerException;
}

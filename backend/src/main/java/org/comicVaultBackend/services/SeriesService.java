package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.SeriesDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.domain.entities.SeriesEntity;

import java.util.List;
import java.util.Optional;

public interface SeriesService {
    SeriesEntity createSeries(SeriesEntity series);

    List<SeriesEntity> listAll();

    List<SeriesEntity> listOnGoingSeries();

    Optional<SeriesEntity> getSeriesByID(String seriesID);

    void deleteSeriesById(String seriesID);


    void save(SeriesEntity seriesEntity);

    ComicEntity getCurrentComicForSeries(SeriesEntity seriesEntity);

    void updateNonNullProperties(SeriesDTO seriesDTO, SeriesEntity seriesEntity) throws IllegalAccessException;
}

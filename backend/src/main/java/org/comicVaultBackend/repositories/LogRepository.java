package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.LogEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LogRepository extends CrudRepository<LogEntity, Long> {
    List<LogEntity> findAllByComicId(String comicId);

    List<LogEntity> findAllBySeriesId(String seriesId);

    List<LogEntity> findAllByJobId(Long JobId);
}


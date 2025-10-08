package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.entities.LogEntity;

import java.util.List;

public interface LogService {

    List<LogEntity> listLogs(int page);

    void save(LogEntity log);

    void removeComicIdFromLog(String comicId);

    void removeSeriesIdFromLog(String comicId);

    List<LogEntity> findAllByJobId(long jobId);
}

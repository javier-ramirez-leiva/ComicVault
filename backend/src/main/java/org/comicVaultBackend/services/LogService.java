package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.entities.LogEntity;
import org.comicVaultBackend.domain.regular.LogFilter;

import java.util.List;

public interface LogService {

    List<LogEntity> listLogs(int page);

    List<LogEntity> filterLogs(int page, LogFilter logFilter);

    void save(LogEntity log);

    void removeComicIdFromLog(String comicId);

    void removeSeriesIdFromLog(String comicId);

    List<LogEntity> findAllByJobId(long jobId);
}

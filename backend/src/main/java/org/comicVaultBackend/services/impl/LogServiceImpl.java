package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.domain.entities.LogEntity;
import org.comicVaultBackend.repositories.LogRepository;
import org.comicVaultBackend.repositories.LogRepositoryPage;
import org.comicVaultBackend.services.LogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;
    private final LogRepositoryPage logRepositoryPage;

    public LogServiceImpl(LogRepository logRepository, LogRepositoryPage logRepositoryPage) {
        this.logRepository = logRepository;
        this.logRepositoryPage = logRepositoryPage;
    }

    @Override
    public List<LogEntity> listLogs(int page) {
        Pageable pageable = PageRequest.of(page, 25, Sort.by(Sort.Direction.DESC, "timeStamp"));
        Page<LogEntity> logs = logRepositoryPage.findAll(pageable);
        return logs.getContent();
    }

    @Override
    public void save(LogEntity log) {
        logRepository.save(log);
    }

    @Override
    public void removeComicIdFromLog(String comicId) {
        List<LogEntity> logs = logRepository.findAllByComicId(comicId);
        for (LogEntity log : logs) {
            log.setComicId(null);
        }
    }

    @Override
    public void removeSeriesIdFromLog(String comicId) {
        List<LogEntity> logs = logRepository.findAllBySeriesId(comicId);
        for (LogEntity log : logs) {
            log.setSeriesId(null);
        }
    }

    @Override
    public List<LogEntity> findAllByJobId(long jobId) {
        return this.logRepository.findAllByJobId(jobId);
    }

}

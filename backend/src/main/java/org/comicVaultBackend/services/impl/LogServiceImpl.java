package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.annotations.CVReadTransaction;
import org.comicVaultBackend.annotations.CVWriteTransaction;
import org.comicVaultBackend.domain.entities.LogEntity;
import org.comicVaultBackend.repositories.LogRepository;
import org.comicVaultBackend.repositories.LogRepositoryPage;
import org.comicVaultBackend.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogRepository logRepository;
    @Autowired
    private LogRepositoryPage logRepositoryPage;


    @Override
    @CVReadTransaction
    public List<LogEntity> listLogs(int page) {
        Pageable pageable = PageRequest.of(page, 25, Sort.by(Sort.Direction.DESC, "timeStamp"));
        Page<LogEntity> logs = logRepositoryPage.findAll(pageable);
        return logs.getContent();
    }

    @Override
    @CVWriteTransaction
    public void save(LogEntity log) {
        logRepository.save(log);
    }

    @Override
    @CVWriteTransaction
    public void removeComicIdFromLog(String comicId) {
        List<LogEntity> logs = logRepository.findAllByComicId(comicId);
        for (LogEntity log : logs) {
            log.setComicId(null);
        }
    }

    @Override
    @CVWriteTransaction
    public void removeSeriesIdFromLog(String comicId) {
        List<LogEntity> logs = logRepository.findAllBySeriesId(comicId);
        for (LogEntity log : logs) {
            log.setSeriesId(null);
        }
    }

    @Override
    @CVReadTransaction
    public List<LogEntity> findAllByJobId(long jobId) {
        return this.logRepository.findAllByJobId(jobId);
    }

}

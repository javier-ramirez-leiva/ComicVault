package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.annotations.CVReadTransaction;
import org.comicVaultBackend.annotations.CVWriteTransaction;
import org.comicVaultBackend.domain.entities.LogEntity;
import org.comicVaultBackend.domain.regular.LogFilter;
import org.comicVaultBackend.repositories.LogRepository;
import org.comicVaultBackend.repositories.LogRepositoryPage;
import org.comicVaultBackend.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    @CVReadTransaction
    public List<LogEntity> filterLogs(int page, LogFilter logFilter) {
        Specification<LogEntity> specification = buildSpecification(logFilter);
        Sort sort = buildSort(logFilter);
        Pageable pageable = PageRequest.of(
                page,
                25,
                sort
        );
        Page<LogEntity> logs = logRepositoryPage.findAll(specification, pageable);
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

    private Specification<LogEntity> buildSpecification(LogFilter filter) {
        if (filter == null) {
            return Specification.where(null);
        }
        Specification<LogEntity> specification = Specification.where(null);
        // Types
        Specification<LogEntity> severitySpecification = null;
        if (filter.getSeverities() != null) {
            if (filter.getSeverities().isError()) {
                severitySpecification = addOr(severitySpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("severity"), LogEntity.LogSeverity.Error));
            }
            if (filter.getSeverities().isInfo()) {
                severitySpecification = addOr(severitySpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("severity"), LogEntity.LogSeverity.Info));
            }
            if (filter.getSeverities().isWarning()) {
                severitySpecification = addOr(severitySpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("severity"), LogEntity.LogSeverity.Warning));
            }
        }
        if (severitySpecification != null) {
            specification = specification.and(severitySpecification);
        }


        // Username
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("username"), filter.getUsername())
            );
        }

        // Message contains
        if (filter.getMessageContains() != null && !filter.getMessageContains().isBlank()) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("message")),
                                    "%" + filter.getMessageContains().toLowerCase() + "%"
                            )
            );
        }

        // Details contains
        if (filter.getDetailsContains() != null && !filter.getDetailsContains().isBlank()) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("details")),
                                    "%" + filter.getDetailsContains().toLowerCase() + "%"
                            )
            );
        }

        // Date start
        if (filter.getDateStart() != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("timeStamp"),
                                    filter.getDateStart()
                            )
            );
        }
        // Date end
        if (filter.getDateEnd() != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get("timeStamp"),
                                    filter.getDateEnd()
                            )
            );
        }
        return specification;
    }

    private Specification<LogEntity> addOr(
            Specification<LogEntity> existing,
            Specification<LogEntity> additional) {
        if (existing == null) {
            return additional;
        }
        return existing.or(additional);
    }

    private Sort buildSort(LogFilter filter) {
        String property = "timeStamp";
        if (filter != null && filter.getLogSortAttribute() != null) {
            property = switch (filter.getLogSortAttribute()) {
                case LATEST -> "timeStamp";
                case USERNAME -> "username";
                case SEVERITY -> "severity";
            };
        }
        Sort.Direction direction =
                filter != null && filter.isSortDescendingDirection()
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        //Invert sorting for timestamp
        if (filter.getLogSortAttribute() == LogFilter.LogSortAttribute.LATEST) {
            direction = direction == Sort.Direction.DESC ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

}

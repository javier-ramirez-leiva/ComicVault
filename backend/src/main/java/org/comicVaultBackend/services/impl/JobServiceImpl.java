package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.annotations.CVReadTransaction;
import org.comicVaultBackend.annotations.CVWriteTransaction;
import org.comicVaultBackend.domain.dto.JobDTO;
import org.comicVaultBackend.domain.entities.JobEntity;
import org.comicVaultBackend.domain.regular.JobFilter;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.repositories.JobRepository;
import org.comicVaultBackend.repositories.JobRepositoryPage;
import org.comicVaultBackend.services.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRepositoryPage jobRepositoryPage;

    @Autowired
    private Mapper<JobEntity, JobDTO> jobMapper;


    @Override
    @CVWriteTransaction
    public JobEntity createJob(JobDTO job) {
        JobEntity jobEntity = jobMapper.mapFrom(job);
        jobEntity.setLogsIds(new ArrayList<>());
        save(jobEntity);
        return jobEntity;
    }

    @Override
    @CVWriteTransaction
    public void save(JobEntity jobEntity) {
        jobRepository.save(jobEntity);
    }

    @Override
    @CVWriteTransaction
    public void finishJob(JobEntity jobEntity, JobEntity.Status status) {
        jobEntity.setStatus(status);
        setDuration(jobEntity);
        save(jobEntity);
    }


    @Override
    @CVWriteTransaction
    public void setDuration(JobEntity jobEntity) {
        Date now = new Date(); // now


        // Convert Date -> LocalDateTime
        LocalDateTime start = LocalDateTime.ofInstant(jobEntity.getTimeStamp().toInstant(), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());

        // Calculate duration
        Duration duration = Duration.between(start, end);

        jobEntity.setDuration(duration.getSeconds());
    }

    @Override
    @CVReadTransaction
    public List<JobEntity> listJobs(int page) {
        Pageable pageable = PageRequest.of(page, 25, Sort.by(Sort.Direction.DESC, "timeStamp"));
        Page<JobEntity> jobs = jobRepositoryPage.findAll(pageable);
        return jobs.getContent();
    }

    @Override
    @CVReadTransaction
    public List<JobEntity> filterJobs(int page, JobFilter jobFilter) {
        Specification<JobEntity> specification = buildSpecification(jobFilter);
        Sort sort = buildSort(jobFilter);
        Pageable pageable = PageRequest.of(
                page,
                25,
                sort
        );
        Page<JobEntity> jobs = jobRepositoryPage.findAll(specification, pageable);
        return jobs.getContent();
    }


    @Override
    @CVReadTransaction
    public boolean areThereJobs(JobEntity.Type type, JobEntity.Status status) {
        return !jobRepository.findAllByTypeAndStatus(type, status).isEmpty();
    }

    @Override
    @CVReadTransaction
    public Optional<JobEntity> getJobById(Long jobId) {
        List<JobEntity> listJobs = jobRepository.findAllByJobId(jobId);
        if (!listJobs.isEmpty()) {
            return Optional.of(listJobs.get(0));
        }
        return Optional.empty();
    }

    @Override
    @CVWriteTransaction
    public void cleanOnGoingJobs() {
        for (JobEntity jobEntity : jobRepository.findAllByStatus(JobEntity.Status.ON_GOING)) {
            jobEntity.setStatus(JobEntity.Status.INTERRUPTED);
            jobRepository.save(jobEntity);
        }
    }

    private Specification<JobEntity> buildSpecification(JobFilter filter) {
        if (filter == null) {
            return Specification.where(null);
        }
        Specification<JobEntity> specification = Specification.where(null);
        // Types
        Specification<JobEntity> typeSpecification = null;
        if (filter.getTypes() != null) {
            if (filter.getTypes().isDownload()) {
                typeSpecification = addOr(typeSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("type"), JobEntity.Type.DOWNLOAD));
            }
            if (filter.getTypes().isDownloadList()) {
                typeSpecification = addOr(typeSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("type"), JobEntity.Type.DOWNLOAD_LIST));
            }
            if (filter.getTypes().isDelete()) {
                typeSpecification = addOr(typeSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("type"), JobEntity.Type.DELETE));
            }
            if (filter.getTypes().isScanLib()) {
                typeSpecification = addOr(typeSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("type"), JobEntity.Type.SCAN_LIB));
            }
            if (filter.getTypes().isCleanLib()) {
                typeSpecification = addOr(typeSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("type"), JobEntity.Type.CLEAN_LIB));
            }
            if (filter.getTypes().isDeleteLib()) {
                typeSpecification = addOr(typeSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("type"), JobEntity.Type.DELETE_LIB));
            }
        }
        if (typeSpecification != null) {
            specification = specification.and(typeSpecification);
        }

        // Status
        Specification<JobEntity> statusSpecification = null;
        if (filter.getStatus() != null) {
            if (filter.getStatus().isOnGoing()) {
                statusSpecification = addOr(statusSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("status"), JobEntity.Status.ON_GOING));
            }
            if (filter.getStatus().isCompleted()) {
                statusSpecification = addOr(statusSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("status"), JobEntity.Status.COMPLETED));
            }
            if (filter.getStatus().isError()) {
                statusSpecification = addOr(statusSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("status"), JobEntity.Status.ERROR));
            }
            if (filter.getStatus().isInterrupted()) {
                statusSpecification = addOr(statusSpecification,
                        (root, query, cb) ->
                                cb.equal(root.get("status"), JobEntity.Status.INTERRUPTED));
            }
        }
        if (statusSpecification != null) {
            specification = specification.and(statusSpecification);
        }

        // Username
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("username"), filter.getUsername())
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

    private Specification<JobEntity> addOr(
            Specification<JobEntity> existing,
            Specification<JobEntity> additional) {
        if (existing == null) {
            return additional;
        }
        return existing.or(additional);
    }

    private Sort buildSort(JobFilter filter) {
        String property = "timeStamp";
        if (filter != null && filter.getJobSortAttribute() != null) {
            property = switch (filter.getJobSortAttribute()) {
                case LATEST -> "timeStamp";
                case STATUS -> "status";
                case TYPE -> "type";
                case DURATION -> "duration";
                case USERNAME -> "username";
            };
        }
        Sort.Direction direction =
                filter != null && filter.isSortDescendingDirection()
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        //Invert sorting for timestamp
        if (filter.getJobSortAttribute() == JobFilter.JobSortAttribute.LATEST) {
            direction = direction == Sort.Direction.DESC ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }
}

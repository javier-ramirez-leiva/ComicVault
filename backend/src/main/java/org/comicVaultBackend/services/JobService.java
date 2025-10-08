package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.JobDTO;
import org.comicVaultBackend.domain.entities.JobEntity;

import java.util.List;
import java.util.Optional;

public interface JobService {

    JobEntity createJob(JobDTO job);

    void save(JobEntity jobEntity);

    void finishJob(JobEntity jobEntity, JobEntity.STATUS status);

    void setDuration(JobEntity jobEntity);

    List<JobEntity> listJobs(int page);

    Optional<JobEntity> getJobById(Long jobId);
}

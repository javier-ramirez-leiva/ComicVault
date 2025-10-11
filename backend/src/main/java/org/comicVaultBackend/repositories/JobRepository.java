package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.JobEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JobRepository extends CrudRepository<JobEntity, Long> {

    List<JobEntity> findAllByJobId(Long jobId);

    List<JobEntity> findAllByTypeAndStatus(JobEntity.Type type, JobEntity.Status status);
}

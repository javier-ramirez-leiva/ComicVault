package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.JobEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JobRepositoryPage extends PagingAndSortingRepository<JobEntity, Long>, JpaSpecificationExecutor<JobEntity> {
}

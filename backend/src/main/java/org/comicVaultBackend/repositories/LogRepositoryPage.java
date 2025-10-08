package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.LogEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LogRepositoryPage extends PagingAndSortingRepository<LogEntity, Long> {
    
}

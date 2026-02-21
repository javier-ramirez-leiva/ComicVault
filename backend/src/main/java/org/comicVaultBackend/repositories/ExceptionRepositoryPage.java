package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.ExceptionEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ExceptionRepositoryPage extends PagingAndSortingRepository<ExceptionEntity, Long> {

}

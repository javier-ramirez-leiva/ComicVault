package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.ExceptionEntity;
import org.springframework.data.repository.CrudRepository;

public interface ExceptionRepository extends CrudRepository<ExceptionEntity, Long> {

}

package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.ProgressEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProgressRepository extends CrudRepository<ProgressEntity, Long> {


}

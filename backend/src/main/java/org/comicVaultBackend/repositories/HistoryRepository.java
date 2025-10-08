package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.HistoryEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends CrudRepository<HistoryEntity, Long> {
    List<HistoryEntity> findAllByUsername (String username);
    Optional<HistoryEntity> findByAutomaticId(Long id);
}

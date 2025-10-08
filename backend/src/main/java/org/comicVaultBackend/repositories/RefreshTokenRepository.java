package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findById(String id);

    void deleteByUsername(String username);

    Optional<RefreshTokenEntity> findByDeviceId(String id);

    List<RefreshTokenEntity> findAllByOrderByCreatedAtDesc();
}

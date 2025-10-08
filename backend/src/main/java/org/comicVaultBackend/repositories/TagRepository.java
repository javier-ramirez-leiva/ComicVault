package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.TagEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends CrudRepository<TagEntity, String> {
    Optional<TagEntity> findByName(String name);
}
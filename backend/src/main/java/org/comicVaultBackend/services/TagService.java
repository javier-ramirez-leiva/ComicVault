package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.entities.TagEntity;

import java.util.Optional;

public interface TagService {
    Optional<TagEntity> findName(String name);

    void save(TagEntity tagEntity);
}

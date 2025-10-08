package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.SchemaVersionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SchemaVersionRepository extends CrudRepository<SchemaVersionEntity, String> {
    List<SchemaVersionEntity> findAll();
}

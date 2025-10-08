package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.ComicEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComicRepository extends CrudRepository<ComicEntity, String> {
    List<ComicEntity> findAllByIdGc(String idGc);

    List<ComicEntity> findAllByOrderByCreatedAtDesc();

}

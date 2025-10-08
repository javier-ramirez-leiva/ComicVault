package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.SeriesEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeriesRepository extends CrudRepository<SeriesEntity, String>{
    List<SeriesEntity> findAllByOrderByModifiedAtDesc();

}




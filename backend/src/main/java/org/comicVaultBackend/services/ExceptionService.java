package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.entities.ExceptionEntity;

import java.util.List;
import java.util.Optional;

public interface ExceptionService {
    public List<ExceptionEntity> listExceptions(int page);

    Optional<ExceptionEntity> getByEntityId(long entityId);
    
    void save(ExceptionEntity exceptionEntity);

    void deleteAll();
}

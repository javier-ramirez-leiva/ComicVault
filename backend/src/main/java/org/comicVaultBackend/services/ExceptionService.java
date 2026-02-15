package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.entities.ExceptionEntity;

import java.util.List;

public interface ExceptionService {
    public List<ExceptionEntity> listExceptions(int page);

    void save(ExceptionEntity exceptionEntity);

    void deleteAll();
}

package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.entities.ProgressEntity;


public interface ProgressService {
    public void save(ProgressEntity progressEntity);
    public void delete(ProgressEntity progressEntity);
}

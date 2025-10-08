package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.entities.HistoryEntity;

import java.util.List;

public interface HistoryService {
    public List<HistoryEntity> listHistoryForUser(String username);
    public void save(HistoryEntity historyEntity);
}

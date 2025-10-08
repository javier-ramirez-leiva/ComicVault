package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.domain.entities.HistoryEntity;
import org.comicVaultBackend.repositories.HistoryRepository;
import org.comicVaultBackend.services.HistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryServiceImpl(HistoryRepository historyRepository) {this.historyRepository = historyRepository;}

    @Override
    public List<HistoryEntity> listHistoryForUser(String username) {
        return historyRepository.findAllByUsername(username);
    }

    @Override
    public void save(HistoryEntity historyEntity) {
        historyRepository.save(historyEntity);
    }
}

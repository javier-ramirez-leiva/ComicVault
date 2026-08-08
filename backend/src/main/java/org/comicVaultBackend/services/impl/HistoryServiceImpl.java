package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.annotations.CVReadTransaction;
import org.comicVaultBackend.annotations.CVWriteTransaction;
import org.comicVaultBackend.domain.entities.HistoryEntity;
import org.comicVaultBackend.repositories.HistoryRepository;
import org.comicVaultBackend.services.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryServiceImpl implements HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    @Override
    @CVReadTransaction
    public List<HistoryEntity> listHistoryForUser(String username) {
        return historyRepository.findAllByUsername(username);
    }

    @Override
    @CVReadTransaction
    public List<HistoryEntity> listHistoryForComicId(String comicId) {
        return historyRepository.findAllByComicId(comicId);
    }

    @Override
    @CVWriteTransaction
    public void save(HistoryEntity historyEntity) {
        historyRepository.save(historyEntity);
    }
}

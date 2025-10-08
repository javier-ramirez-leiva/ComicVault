package org.comicVaultBackend.services.impl;


import org.comicVaultBackend.domain.entities.HistoryEntity;
import org.comicVaultBackend.domain.entities.ProgressEntity;
import org.comicVaultBackend.repositories.HistoryRepository;
import org.comicVaultBackend.repositories.ProgressRepository;
import org.comicVaultBackend.services.ProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final HistoryRepository historyRepository;

    public ProgressServiceImpl(ProgressRepository progressRepository, HistoryRepository historyRepository) {
        this.progressRepository = progressRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void save(ProgressEntity progressEntity) {
        progressRepository.save(progressEntity);
        HistoryEntity historyEntity = HistoryEntity.builder().
                automaticId(progressEntity.getAutomaticId()).
                progress_date(progressEntity.getProgress_date()).
                username(progressEntity.getUser().getUsername()).
                comicTitle(progressEntity.getComic().getTitle()).
                comicId(progressEntity.getComic().getId()).
                pageStatus(progressEntity.getPageStatus()).
                readStatus(progressEntity.getReadStatus()).
                pages(progressEntity.getComic().getPages()).
                alive(true).
                build();
        historyRepository.save(historyEntity);
    }

    @Override
    @Transactional
    public void delete(ProgressEntity progressEntity) {
        Optional<HistoryEntity> historyEntityOptional = historyRepository.findByAutomaticId(progressEntity.getAutomaticId());
        historyEntityOptional.ifPresent(historyEntity -> historyEntity.setAlive(false));
        progressRepository.delete(progressEntity);
    }
}

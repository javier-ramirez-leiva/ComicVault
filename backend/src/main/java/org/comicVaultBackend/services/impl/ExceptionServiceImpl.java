package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.domain.entities.ExceptionEntity;
import org.comicVaultBackend.repositories.ExceptionRepository;
import org.comicVaultBackend.repositories.ExceptionRepositoryPage;
import org.comicVaultBackend.services.ExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExceptionServiceImpl implements ExceptionService {
    @Autowired
    private ExceptionRepositoryPage exceptionRepositoryPage;

    @Autowired
    private ExceptionRepository exceptionRepository;


    @Override
    public List<ExceptionEntity> listExceptions(int page) {
        Pageable pageable = PageRequest.of(page, 25, Sort.by(Sort.Direction.DESC, "timeStamp"));
        Page<ExceptionEntity> jobs = exceptionRepositoryPage.findAll(pageable);
        return jobs.getContent();
    }

    @Override
    public void save(ExceptionEntity exceptionEntity) {
        exceptionRepository.save(exceptionEntity);
    }

    @Override
    public void deleteAll() {
        exceptionRepository.deleteAll();
        ;
    }
}

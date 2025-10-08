package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.domain.entities.TagEntity;
import org.comicVaultBackend.repositories.TagRepository;
import org.comicVaultBackend.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagRepository tagRepository;


    @Override
    public Optional<TagEntity> findName(String name) {
        return tagRepository.findByName(name);
    }

    @Override
    public void save(TagEntity tagEntity) {
        tagRepository.save(tagEntity);
    }
}

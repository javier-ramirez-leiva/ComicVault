package org.comicVaultBackend.mappers.impl;

import jakarta.annotation.PostConstruct;
import org.comicVaultBackend.domain.dto.ComicDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.ComicService;
import org.comicVaultBackend.services.ConfigurationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComicMapperImpl implements Mapper<ComicEntity, ComicDTO> {
    private final ModelMapper modelMapper;
    @Autowired
    private ComicService comicService;

    @Autowired
    private ConfigurationService configurationService;

    public ComicMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void initializeMapper() {
        modelMapper.typeMap(ComicEntity.class, ComicDTO.class).addMapping(ComicEntity::getIdGc, ComicDTO::setIdGc);
        modelMapper.typeMap(ComicDTO.class, ComicEntity.class).addMapping(ComicDTO::getIdGc, ComicEntity::setIdGc);
    }

    @Override

    public ComicDTO mapTo(Object entity) {
        ComicEntity comicEntity = (ComicEntity) entity;
        ComicDTO comicDto = modelMapper.map(comicEntity, ComicDTO.class);
        //Fix the linkif relative
        if (!comicDto.getLink().isEmpty() && !comicDto.getLink().startsWith("http")) {
            comicDto.setLink(configurationService.getConfiguration().getGetComicsBaseUrl() + comicEntity.getLink());
        }


        comicDto.setPageStatus(comicEntity.getPageStatus());
        comicDto.setReadStatus(comicEntity.getReadStatus());

        return comicDto;
    }

    @Override
    public ComicEntity mapFrom(ComicDTO comicDto) {
        ComicEntity comicEntity = modelMapper.map(comicDto, ComicEntity.class);
        if (comicDto.getReadStatus()) {
            comicService.setReadStatus(comicEntity, true);
        }
        if (comicDto.getPageStatus() > 0) {
            comicService.setPageStatus(comicEntity, comicDto.getPageStatus());
        }


        return comicEntity;
    }
}

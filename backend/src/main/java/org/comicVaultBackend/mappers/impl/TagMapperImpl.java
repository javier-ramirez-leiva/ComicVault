package org.comicVaultBackend.mappers.impl;

import org.comicVaultBackend.domain.dto.TagDTO;
import org.comicVaultBackend.domain.entities.TagEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class TagMapperImpl implements Mapper<TagEntity, TagDTO> {
    private final ModelMapper modelMapper;

    public TagMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    @Override
    public TagDTO mapTo(Object tagEntity) {
        return modelMapper.map(tagEntity, TagDTO.class);
    }

    @Override
    public TagEntity mapFrom(TagDTO tagDTO) {
        return modelMapper.map(tagDTO, TagEntity.class);
    }
}

package org.comicVaultBackend.mappers.impl;

import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.domain.dto.ProgressDTO;
import org.comicVaultBackend.domain.entities.ProgressEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ProgressMapperImpl implements Mapper<ProgressEntity, ProgressDTO> {

    private final ModelMapper modelMapper;

    public ProgressMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ProgressDTO mapTo(Object progressEntity) {
        return modelMapper.map(progressEntity, ProgressDTO.class);
    }

    @Override
    public ProgressEntity mapFrom(ProgressDTO progressDto) {
        return modelMapper.map(progressDto, ProgressEntity.class);
    }
}


package org.comicVaultBackend.mappers.impl;

import org.comicVaultBackend.domain.dto.LogDTO;
import org.comicVaultBackend.domain.entities.LogEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LogMapperImpl implements Mapper<LogEntity, LogDTO> {
    private final ModelMapper modelMapper;

    public LogMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    @Override
    public LogDTO mapTo(Object logEntity) {
        return modelMapper.map(logEntity, LogDTO.class);
    }

    @Override
    public LogEntity mapFrom(LogDTO logDTO) {
        return modelMapper.map(logDTO, LogEntity.class);
    }
}


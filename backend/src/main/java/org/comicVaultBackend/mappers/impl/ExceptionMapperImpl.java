package org.comicVaultBackend.mappers.impl;

import org.comicVaultBackend.domain.dto.ExceptionDTO;
import org.comicVaultBackend.domain.entities.ExceptionEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ExceptionMapperImpl implements Mapper<ExceptionEntity, ExceptionDTO> {
    private final ModelMapper modelMapper;

    public ExceptionMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    @Override
    public ExceptionDTO mapTo(Object exceptionEntity) {
        return modelMapper.map(exceptionEntity, ExceptionDTO.class);
    }

    @Override
    public ExceptionEntity mapFrom(ExceptionDTO exceptionDTO) {
        return modelMapper.map(exceptionDTO, ExceptionEntity.class);
    }
}
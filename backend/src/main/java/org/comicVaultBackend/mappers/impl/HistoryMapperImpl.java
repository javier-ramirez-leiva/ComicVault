package org.comicVaultBackend.mappers.impl;

import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.domain.dto.HistoryDTO;
import org.comicVaultBackend.domain.entities.HistoryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class HistoryMapperImpl implements Mapper<HistoryEntity, HistoryDTO> {

    private final ModelMapper modelMapper;

    public HistoryMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public HistoryDTO mapTo(Object progressEntity) {
        return modelMapper.map(progressEntity, HistoryDTO.class);
    }

    @Override
    public HistoryEntity mapFrom(HistoryDTO progressDto) {
        return modelMapper.map(progressDto, HistoryEntity.class);
    }
}

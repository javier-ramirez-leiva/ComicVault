package org.comicVaultBackend.mappers.impl;

import jakarta.annotation.PostConstruct;
import org.comicVaultBackend.domain.dto.SeriesDTO;
import org.comicVaultBackend.domain.entities.SeriesEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
public class SeriesMapperImpl implements Mapper<SeriesEntity, SeriesDTO> {
    private final ModelMapper modelMapper;

    public SeriesMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void initializeMapper() {
        modelMapper.typeMap(SeriesEntity.class, SeriesDTO.class).addMapping(SeriesEntity::getSeriesID, SeriesDTO::setId);
        modelMapper.typeMap(SeriesDTO.class, SeriesEntity.class).addMapping(SeriesDTO::getId, SeriesEntity::setSeriesID);
    }


    @Override
    public SeriesDTO mapTo(Object authorEntity) {
        return modelMapper.map(authorEntity, SeriesDTO.class);
    }

    @Override
    public SeriesEntity mapFrom(SeriesDTO authorDto) {
        return modelMapper.map(authorDto, SeriesEntity.class);
    }
}

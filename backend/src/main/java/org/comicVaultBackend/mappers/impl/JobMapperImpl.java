package org.comicVaultBackend.mappers.impl;

import org.comicVaultBackend.domain.dto.JobDTO;
import org.comicVaultBackend.domain.entities.JobEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class JobMapperImpl implements Mapper<JobEntity, JobDTO> {
    private final ModelMapper modelMapper;

    public JobMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    @Override
    public JobDTO mapTo(Object jobEntity) {
        return modelMapper.map(jobEntity, JobDTO.class);
    }

    @Override
    public JobEntity mapFrom(JobDTO jobDTO) {
        return modelMapper.map(jobDTO, JobEntity.class);
    }
}

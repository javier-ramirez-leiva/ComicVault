package org.comicVaultBackend.mappers.impl;

import org.comicVaultBackend.domain.entities.UserEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.domain.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;



@Component
public class UserMapperImpl implements Mapper<UserEntity, UserDTO> {

    private final ModelMapper modelMapper;

    public UserMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    @Override
    public UserDTO mapTo(Object userEntity) {
        return modelMapper.map(userEntity, UserDTO.class);
    }

    @Override
    public UserEntity mapFrom(UserDTO userDTO) {
        return modelMapper.map(userDTO, UserEntity.class);
    }



}
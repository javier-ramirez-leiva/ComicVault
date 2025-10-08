package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.UserDTO;
import org.comicVaultBackend.domain.entities.UserEntity;
import org.comicVaultBackend.domain.regular.UserInfoResponse;


import java.util.List;
import java.util.Optional;

public interface UserService {
    void createUser(UserEntity user);

    List<UserEntity> listAll();

    Optional<UserEntity> findByUsername(String username);

    boolean adminUserExists();

    int howManyAdmins();

    void save(UserEntity user);

    void delete(UserEntity user);

    UserInfoResponse fromUserToInfo (UserDTO userDTO);
}

package org.comicVaultBackend.repositories;

import org.comicVaultBackend.domain.entities.UserEntity;
import org.comicVaultBackend.domain.regular.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findFirstByRole(Role role);
    List<UserEntity>  findAllByOrderByCreatedAtDesc();
    List<UserEntity> findAllByRole(Role role);
}

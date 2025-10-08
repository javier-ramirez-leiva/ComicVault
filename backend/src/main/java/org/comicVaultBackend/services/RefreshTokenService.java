package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.LoggedDeviceInfoDTO;
import org.comicVaultBackend.domain.entities.RefreshTokenEntity;

import java.util.List;
import java.util.Optional;


public interface RefreshTokenService {

    List<RefreshTokenEntity> listAll();

    Optional<RefreshTokenEntity> findByDeviceId(String deviceId);

    void deleteRefreshToken(RefreshTokenEntity refreshTokenEntity);

    void deleteByUsername(String username);

    void save(RefreshTokenEntity tokenEntity);

    LoggedDeviceInfoDTO mapTo(RefreshTokenEntity refreshTokenEntity);
}

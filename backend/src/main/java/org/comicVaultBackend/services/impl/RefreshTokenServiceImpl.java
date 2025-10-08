package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.config.JwtUtil;
import org.comicVaultBackend.domain.dto.LoggedDeviceInfoDTO;
import org.comicVaultBackend.domain.entities.RefreshTokenEntity;
import org.comicVaultBackend.repositories.RefreshTokenRepository;
import org.comicVaultBackend.services.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public List<RefreshTokenEntity> listAll() {
        return new ArrayList<>(refreshTokenRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public Optional<RefreshTokenEntity> findByDeviceId(String deviceId) {
        return this.refreshTokenRepository.findByDeviceId(deviceId);
    }

    @Override
    @Transactional
    public void deleteRefreshToken(RefreshTokenEntity refreshTokenEntity) {
        this.refreshTokenRepository.delete(refreshTokenEntity);
        logger.info("Delete refresh token: {}", refreshTokenEntity.getDevice());
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        this.refreshTokenRepository.deleteByUsername(username);
        logger.info("Delete all refresh tokens of user: {}", username);
    }

    @Override
    public void save(RefreshTokenEntity tokenEntity) {
        refreshTokenRepository.save(tokenEntity);
        logger.info("Added refresh token: {}", tokenEntity.getDevice());
    }

    @Override
    public LoggedDeviceInfoDTO mapTo(RefreshTokenEntity refreshTokenEntity) {
        return LoggedDeviceInfoDTO.builder()
                .id(refreshTokenEntity.getDeviceId())
                .username(refreshTokenEntity.getUsername())
                .userAgent(refreshTokenEntity.getUserAgent())
                .os(refreshTokenEntity.getOs())
                .browser(refreshTokenEntity.getBrowser())
                .device(refreshTokenEntity.getDevice())
                .osVersion(refreshTokenEntity.getOsVersion())
                .browserVersion(refreshTokenEntity.getBrowserVersion())
                .orientation(refreshTokenEntity.getOrientation())
                .createdAt(refreshTokenEntity.getCreatedAt())
                .ip(refreshTokenEntity.getIp())
                .build();
    }
}

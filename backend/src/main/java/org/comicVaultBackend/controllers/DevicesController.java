package org.comicVaultBackend.controllers;

import org.comicVaultBackend.domain.dto.LoggedDeviceInfoDTO;
import org.comicVaultBackend.domain.entities.RefreshTokenEntity;
import org.comicVaultBackend.exceptions.EntityNotFoundException;
import org.comicVaultBackend.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.version}/devices")
public class DevicesController {
    @Autowired
    private RefreshTokenService refreshTokenService;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("")
    public List<LoggedDeviceInfoDTO> getUsers() {
        List<RefreshTokenEntity> refreshTokens = refreshTokenService.listAll();
        return refreshTokens.stream()
                .map(refreshTokenService::mapTo)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping(path = "{deviceId}")
    public void deleteRefreshToken(@PathVariable String deviceId) throws EntityNotFoundException, IllegalArgumentException {
        Optional<RefreshTokenEntity> refreshTokenEntity = refreshTokenService.findByDeviceId(deviceId);

        if (refreshTokenEntity.isEmpty()) {
            throw new EntityNotFoundException(deviceId, EntityNotFoundException.Entity.DEVICES);
        }
        refreshTokenService.deleteRefreshToken(refreshTokenEntity.get());
    }
}

package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthentificationRequestDTO {
    private String username;
    private String password;

    private String userAgent;
    private String os;
    private String browser;
    private String device;
    private String osVersion;
    private String browserVersion;
    private String orientation;
}

package org.comicVaultBackend.config;

import org.springframework.beans.factory.annotation.Value;

public class JwtSecrets {
    @Value("${JWT_ACCESS_SECRET}")
    private String accessSecret;

    @Value("${JWT_REFRESH_SECRET}")
    private String refreshSecret;

    public String getAccessSecret() {
        return accessSecret;
    }

    public String getRefreshSecret() {
        return refreshSecret;
    }
}

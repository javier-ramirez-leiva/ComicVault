package org.comicVaultBackend.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1.0.0")
                .packagesToScan("org.comicVaultBackend.controllers")
                .pathsToMatch("/api/v1.0.0/**")
                .build();

    }
}

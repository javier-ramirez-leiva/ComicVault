package org.comicVaultBackend.controllers;

import org.comicVaultBackend.config.ApiConfig;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.version}/")
public class GeneralController {

    private final ApiConfig apiConfig;

    public GeneralController(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        return response;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/version")
    public Map<String, String> version() {
        Map<String, String> response = new HashMap<>();
        response.put("number", "1.0.0");
        response.put("type", "java");
        return response;
    }

}

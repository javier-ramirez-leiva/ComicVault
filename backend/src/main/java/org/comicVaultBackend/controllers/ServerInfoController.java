package org.comicVaultBackend.controllers;


import org.comicVaultBackend.domain.dto.ServerInfoDTO;
import org.comicVaultBackend.domain.entities.SchemaVersionEntity;
import org.comicVaultBackend.repositories.SchemaVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.GitProperties;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.version}")
public class ServerInfoController {

    private final GitProperties gitProperties;

    @Autowired
    private SchemaVersionRepository schemaVersionRepository;

    @Value("${api.version}")
    private String apiVersion;

    public ServerInfoController(@Nullable GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @GetMapping("/serverInfo")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ServerInfoDTO serverInfo() {
        List<SchemaVersionEntity> all = this.schemaVersionRepository.findAll();
        String schemaVersion = all.isEmpty() ? "unknown" : all.get(0).getSchemaVersionID();

        String commitId = gitProperties != null ? gitProperties.getCommitId() : "";
        String branch = gitProperties != null ? gitProperties.getBranch() : "";
        String commitTime = gitProperties != null && gitProperties.getCommitTime() != null
                ? gitProperties.getCommitTime().toString()
                : "";

        String commitMessagePackage = gitProperties != null && gitProperties.get("commit.message.short") != null
                ? gitProperties.get("commit.message.short")
                : "";
        return ServerInfoDTO.builder()
                .commitId(commitId)
                .branch(branch)
                .commitTime(commitTime)
                .commitMessage(commitMessagePackage)
                .schemaVersion(schemaVersion)
                .apiVersion(apiVersion)
                .build();
    }
}


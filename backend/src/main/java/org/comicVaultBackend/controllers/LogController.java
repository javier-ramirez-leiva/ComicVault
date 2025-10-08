package org.comicVaultBackend.controllers;

import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.LogDTO;
import org.comicVaultBackend.domain.entities.LogEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.version}/")
public class LogController {

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private LogService logService;

    @Autowired
    private Mapper<LogEntity, LogDTO> logMappger;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/logs")
    public List<LogDTO> logs(@RequestParam(name = "page", required = true, defaultValue = "1") int page) {

        List<LogEntity> logs = logService.listLogs(page - 1);
        List<LogDTO> listLogs = logs.stream()
                .map(logMappger::mapTo)
                .collect(Collectors.toList());
        return listLogs;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/logs/search")
    public List<LogDTO> searchLogs(@RequestParam(name = "jobId", required = true) Long jobId) {
        List<LogEntity> logs = logService.findAllByJobId(jobId);
        List<LogDTO> listLogs = logs.stream()
                .map(logMappger::mapTo)
                .collect(Collectors.toList());
        return listLogs;
    }


}

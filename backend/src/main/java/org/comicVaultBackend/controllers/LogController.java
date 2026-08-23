package org.comicVaultBackend.controllers;

import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.LogDTO;
import org.comicVaultBackend.domain.entities.LogEntity;
import org.comicVaultBackend.domain.regular.LogFilter;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/logs")
    public List<LogDTO> logs(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                             @RequestBody(required = false) LogFilter filter) {

        List<LogEntity> logs = logService.filterLogs(page - 1, filter);
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

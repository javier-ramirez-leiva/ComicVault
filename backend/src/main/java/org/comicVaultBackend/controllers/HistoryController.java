package org.comicVaultBackend.controllers;

import org.comicVaultBackend.domain.dto.HistoryDTO;
import org.comicVaultBackend.domain.entities.HistoryEntity;
import org.comicVaultBackend.domain.regular.FilterHistory;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.version}")
public class HistoryController {
    @Autowired
    private HistoryService historyService;

    @Autowired
    private Mapper<HistoryEntity, HistoryDTO> historyMapper;

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @PostMapping(path = "/history/me")
    public List<HistoryDTO> listHistoryForUser(@RequestBody FilterHistory filterHistory) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ;

        List<HistoryEntity> userHistories = historyService.listHistoryForUser(username);
        return userHistories.stream()
                .filter(filterHistory::filterHistory)
                .sorted(Comparator.comparing(HistoryEntity::getProgress_date).reversed())
                .map(historyMapper::mapTo)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/history/user")
    public List<HistoryDTO> listHistoryForUsername(@RequestParam(name = "username", required = true) String username, @RequestBody FilterHistory filterHistory) {

        List<HistoryEntity> userHistories = historyService.listHistoryForUser(username);
        return userHistories.stream()
                .filter(filterHistory::filterHistory)
                .sorted(Comparator.comparing(HistoryEntity::getProgress_date).reversed())
                .map(historyMapper::mapTo)
                .collect(Collectors.toList());
    }

}

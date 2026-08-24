package org.comicVaultBackend.controllers;

import org.comicVaultBackend.exceptions.ComicVineException;
import org.comicVaultBackend.services.ComicVineService;
import org.comicVaultBackend.services.ConfigurationService;
import org.comicVaultBackend.services.impl.ComicVineServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("${api.version}")
public class ComicVineController {

    @Autowired
    ComicVineService comicVineService;

    @Autowired
    private ConfigurationService configurationService;

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/comicVine/search")
    public Optional<ComicVineServiceImpl.ComicVineIssueInfo> searchIssue(@RequestParam(name = "issue", required = true) String issue) throws ComicVineException {
        return this.comicVineService.searchIssue(issue, this.configurationService.getConfiguration().getComicVineAPIKey());
    }

}

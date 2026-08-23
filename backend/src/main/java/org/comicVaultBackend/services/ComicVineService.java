package org.comicVaultBackend.services;

import org.comicVaultBackend.services.impl.ComicVineServiceImpl;

import java.util.Optional;

public interface ComicVineService {
    Optional<ComicVineServiceImpl.ComicVineIssueInfo> searchIssue(String issueName, String apiKey);
}

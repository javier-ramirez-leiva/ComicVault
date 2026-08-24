package org.comicVaultBackend.services;

import org.comicVaultBackend.exceptions.ComicVineException;
import org.comicVaultBackend.services.impl.ComicVineServiceImpl;

import java.util.Optional;

public interface ComicVineService {
    boolean test(String apiKey) throws ComicVineException;

    Optional<ComicVineServiceImpl.ComicVineIssueInfo> searchIssue(String issueName, String apiKey);
}

package org.comicVaultBackend.services;


import org.comicVaultBackend.domain.regular.ComicTitle;
import org.comicVaultBackend.exceptions.ComicTitleParserServiceException;

public interface ComicTitleParserService {
    ComicTitle parseTitle(String title);

    ComicTitle parseIssueTitle(String title);

    String GetYearDetailsFromStandardTitle(String title) throws ComicTitleParserServiceException;
}

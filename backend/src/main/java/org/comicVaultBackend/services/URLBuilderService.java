package org.comicVaultBackend.services;

public interface URLBuilderService {
    String baseURL(int page);

    String search(String query, int page);

    String tag(String tag, int page);

    String category(String category, int page);

    void setBaseURL(String baseURL);
}

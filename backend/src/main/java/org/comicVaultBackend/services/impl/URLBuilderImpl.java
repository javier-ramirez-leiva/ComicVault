package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.services.URLBuilderService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class URLBuilderImpl implements URLBuilderService {

    private String _baseURL = "";

    @Override
    public void setBaseURL(String baseURL) {
        _baseURL = baseURL;
    }

    @Override
    public String baseURL(int page) {
        if (page != 1)
            return _baseURL + "/page/" + page + "/";
        else
            return _baseURL;
    }

    @Override
    public String search(String query, int page) {
        if (page != 1)
            return _baseURL + "/page/" + page + "/?s=" + _FixQuery((query));
        else
            return _baseURL + "/?s=" + _FixQuery((query));
    }

    @Override
    public String tag(String tag, int page) {
        if (page != 1)
            return _baseURL + "/tag/" + tag + "/page/" + page;
        else
            return _baseURL + "/tag/" + tag;
    }

    @Override
    public String category(String category, int page) {
        if (page != 1)
            return _baseURL + "/cat/" + category + "/page/" + page;
        else
            return _baseURL + "/cat/" + category;
    }

    private String _FixQuery(String query) {
        Map<String, String> fixes = new HashMap<>();
        fixes.put(" ", "+");
        fixes.put("&", "&amp");
        fixes.put("!", "!");
        fixes.put("xmen", "x-men");
        fixes.put("spiderm", "spider-m");

        for (Map.Entry<String, String> entry : fixes.entrySet()) {
            query = query.replace(entry.getKey(), entry.getValue());
        }
        return query;
    }
}

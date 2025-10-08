package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.ComicSearchDTO;
import org.comicVaultBackend.domain.dto.ComicSearchDetailsLinksDTO;
import org.comicVaultBackend.exceptions.*;

import java.util.List;


public interface GetComicsScrapperService {

    void setBaseURL(String baseURL);

    List<ComicSearchDTO> getComics(String url, int page) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException;

    ComicSearchDetailsLinksDTO getComicDetails(String url) throws ComicScrapperParsingException, ComicScrapperGatewayException;

    ComicSearchDTO getCachedComicSearchByIdgc(String idgc) throws EntityNotFoundException;

    ComicSearchDetailsLinksDTO getComicDetailsFromIDgc(String idGc) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperUntreatedException;

}

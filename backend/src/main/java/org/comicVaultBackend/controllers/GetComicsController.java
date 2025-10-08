package org.comicVaultBackend.controllers;

import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.ComicSearchDTO;
import org.comicVaultBackend.domain.dto.ComicSearchDetailsLinksDTO;
import org.comicVaultBackend.domain.dto.ScrapperResponseDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.exceptions.*;
import org.comicVaultBackend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${api.version}")
public class GetComicsController {

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private GetComicsScrapperService getComicsScrapperService;

    @Autowired
    private URLBuilderService urlBuilderService;

    @Autowired
    private ComicService comicService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private ConfigurationService configurationService;

    private void setBaseUrl() {
        urlBuilderService.setBaseURL(configurationService.getConfiguration().getGetComicsBaseUrl());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/search")
    public ScrapperResponseDTO search(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "pageRatio", required = false, defaultValue = "1") int pageRatio,
            @RequestParam(name = "query", required = false, defaultValue = "") String query
    ) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {

        setBaseUrl();
        List<ComicSearchDTO> allComics = new ArrayList<>();
        boolean endReached = false;
        for (int i = 1; i <= pageRatio; ++i) {
            int currentPage = ((page - 1) * pageRatio) + i;
            String url = urlBuilderService.search(query, currentPage);
            try {
                List<ComicSearchDTO> listComics = getComicsScrapperService.getComics(url, currentPage);
                listComics.forEach(this::setDownloadingStatus);
                allComics.addAll(listComics);
            } catch (ComicScrapperParsingException | ComicScrapperGatewayException |
                     ComicScrapperGatewayPageException e) {
                //Scrapping errors can happen here
                if (i == 1 && e instanceof ComicScrapperGatewayException) {
                    throw e;
                } else {
                    endReached = true;
                    break;
                }
            }
        }
        return ScrapperResponseDTO.builder().comicsSearchs(allComics).endReached(endReached).build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/tag")
    public ScrapperResponseDTO tag(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "pageRatio", required = false, defaultValue = "1") int pageRatio,
            @RequestParam(name = "tag", required = false, defaultValue = "") String tag
    ) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {

        setBaseUrl();
        List<ComicSearchDTO> allComics = new ArrayList<>();
        boolean endReached = false;
        for (int i = 1; i <= pageRatio; ++i) {
            int currentPage = ((page - 1) * pageRatio) + i;
            String url = urlBuilderService.tag(tag, currentPage);
            try {
                List<ComicSearchDTO> listComics = getComicsScrapperService.getComics(url, currentPage);
                listComics.forEach(this::setDownloadingStatus);
                allComics.addAll(listComics);
            } catch (ComicScrapperParsingException | ComicScrapperGatewayException |
                     ComicScrapperGatewayPageException e) {
                if (i == 1) {
                    throw e;
                } else {
                    break;
                }
            }
        }
        return ScrapperResponseDTO.builder().comicsSearchs(allComics).endReached(endReached).build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/latest")
    public ScrapperResponseDTO latest(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "pageRatio", required = false, defaultValue = "1") int pageRatio
    ) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {
        setBaseUrl();
        List<ComicSearchDTO> allComics = new ArrayList<>();
        boolean endReached = false;
        for (int i = 1; i <= pageRatio; ++i) {
            int currentPage = ((page - 1) * pageRatio) + i;
            String url = urlBuilderService.baseURL(currentPage);
            try {
                List<ComicSearchDTO> listComics = getComicsScrapperService.getComics(url, currentPage);
                listComics.forEach(this::setDownloadingStatus);
                allComics.addAll(listComics);
            } catch (ComicScrapperParsingException | ComicScrapperGatewayException |
                     ComicScrapperGatewayPageException e) {
                if (i == 1) {
                    throw e;
                } else {
                    break;
                }
            }
        }
        return ScrapperResponseDTO.builder().comicsSearchs(allComics).endReached(endReached).build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/category")
    public ScrapperResponseDTO category(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "pageRatio", required = false, defaultValue = "1") int pageRatio,
            @RequestParam(name = "category", required = false, defaultValue = "") String category
    ) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {
        setBaseUrl();
        List<ComicSearchDTO> allComics = new ArrayList<>();
        boolean endReached = false;
        for (int i = 1; i <= pageRatio; ++i) {
            int currentPage = ((page - 1) * pageRatio) + i;
            String url = urlBuilderService.category(category, ((page - 1) * pageRatio) + i);
            try {
                List<ComicSearchDTO> listComics = getComicsScrapperService.getComics(url, currentPage);
                listComics.forEach(this::setDownloadingStatus);
                allComics.addAll(listComics);
            } catch (ComicScrapperParsingException | ComicScrapperGatewayException |
                     ComicScrapperGatewayPageException e) {
                if (i == 1) {
                    throw e;
                } else {
                    break;
                }
            }
        }
        return ScrapperResponseDTO.builder().comicsSearchs(allComics).endReached(endReached).build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/searchs/{idGc}/details")
    public ComicSearchDetailsLinksDTO details(@PathVariable String idGc) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperUntreatedException {
        ComicSearchDetailsLinksDTO comicSearchDetailsDto = getComicsScrapperService.getComicDetailsFromIDgc(idGc);
        setDownloadingStatus(comicSearchDetailsDto);
        return comicSearchDetailsDto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/searchs/search")
    public ComicSearchDTO getComicSearchByIdgc(@RequestParam(name = "idGc", required = true) String idGc) throws EntityNotFoundException {
        ComicSearchDTO comicSearchDto = getComicsScrapperService.getCachedComicSearchByIdgc(idGc);
        setDownloadingStatus(comicSearchDto);
        return comicSearchDto;

    }

    private void setDownloadingStatus(ComicSearchDTO comicSearchDto) {
        Optional<ComicEntity> comicEntity = comicService.getcomicbyidGc(comicSearchDto.getIdGc());
        //Series are never considered as download. To be improved
        if (comicEntity.isPresent() && (comicEntity.get().getIdGcIssue() == null || comicEntity.get().getIdGcIssue().isBlank())) {
            comicSearchDto.setDownloadingStatus("downloaded");
        } else {
            for (ComicSearchDTO comic : downloadService.getListDownloadingComics()) {
                if (comic.getIdGc().equals(comicSearchDto.getIdGc())) {
                    comicSearchDto.setDownloadingStatus("downloading");
                    comicSearchDto.setTotalBytes(comic.getTotalBytes());
                    comicSearchDto.setCurrentBytes(comic.getCurrentBytes());
                }
            }
        }
    }
}

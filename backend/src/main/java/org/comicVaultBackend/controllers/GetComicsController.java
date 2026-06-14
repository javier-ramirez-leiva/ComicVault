package org.comicVaultBackend.controllers;

import com.google.common.util.concurrent.RateLimiter;
import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.ComicSearchDTO;
import org.comicVaultBackend.domain.dto.ComicSearchDetailsLinksDTO;
import org.comicVaultBackend.domain.dto.ScrapperResponseDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.domain.entities.ExceptionEntity;
import org.comicVaultBackend.exceptions.*;
import org.comicVaultBackend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Autowired
    private ExceptionService exceptionService;

    private enum SearchType {
        BASE_URL,
        SEARCH,
        TAG,
        CATEGORY
    }

    private void setBaseUrl() {
        urlBuilderService.setBaseURL(configurationService.getConfiguration().getGetComicsConfiguration().getBaseUrl());
    }

//    private ScrapperResponseDTO scrappeComics(int page, int pageRatio, long spaceBetweenThreadsMs, SearchType searchType, String queryString) throws ComicScrapperGatewayException {
//        ExecutorService executor = Executors.newFixedThreadPool(pageRatio);
//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//
//        // One future per page, indexed by i (1..pageRatio)
//        List<CompletableFuture<List<ComicSearchDTO>>> futures = new ArrayList<>();
//
//        for (int i = 1; i <= pageRatio; i++) {
//            final int index = i;
//            final int currentPage = ((page - 1) * pageRatio) + index;
//            String url = "";
//            switch (searchType) {
//                case BASE_URL -> url = urlBuilderService.baseURL(currentPage);
//                case SEARCH -> url = urlBuilderService.search(queryString, currentPage);
//                case TAG -> url = urlBuilderService.tag(queryString, currentPage);
//                case CATEGORY -> url = urlBuilderService.category(queryString, currentPage);
//            }
//
//            String finalUrl = url;
//            CompletableFuture<List<ComicSearchDTO>> future = new CompletableFuture<>();
//
//            scheduler.schedule(() -> {
//                CompletableFuture
//                        .supplyAsync(() -> {
//                            try {
//                                List<ComicSearchDTO> comics =
//                                        getComicsScrapperService.getComics(finalUrl, currentPage);
//                                comics.forEach(this::setDownloadingStatus);
//                                return comics;
//                            } catch (ComicScrapperParsingException |
//                                     ComicScrapperGatewayException |
//                                     ComicScrapperGatewayPageException e) {
//                                throw new CompletionException(e);
//                            }
//                        }, executor)
//                        .whenComplete((result, ex) -> {
//                            if (ex != null) {
//                                future.completeExceptionally(ex);
//                            } else {
//                                future.complete(result);
//                            }
//                        });
//            }, (long) (i - 1) * spaceBetweenThreadsMs, TimeUnit.MILLISECONDS);
//
//            futures.add(future);
//        }
//
//        List<ComicSearchDTO> allComics = new ArrayList<>();
//        boolean endReached = false;
//
//        // Rebuild results in page order
//        for (int i = 0; i < futures.size(); i++) {
//            try {
//                List<ComicSearchDTO> pageComics = futures.get(i).join();
//                allComics.addAll(pageComics);
//            } catch (CompletionException e) {
//                Throwable cause = e.getCause();
//
//                if (i == 0 && cause instanceof ComicScrapperGatewayException) {
//                    throw (ComicScrapperGatewayException) cause;
//                }
//
//                endReached = true;
//                break;
//            }
//        }
//
//        executor.shutdown();
//
//        return ScrapperResponseDTO.builder()
//                .comicsSearchs(allComics)
//                .endReached(endReached)
//                .build();
//    }

    private ScrapperResponseDTO scrappeComics(int page, int pageRatio, double requestPerSecond, SearchType searchType, String queryString) throws ComicScrapperGatewayException {
        ExecutorService executor = Executors.newFixedThreadPool(pageRatio);

        RateLimiter rateLimiter = RateLimiter.create(requestPerSecond);

        List<CompletableFuture<List<ComicSearchDTO>>> futures = new ArrayList<>();

        for (int i = 1; i <= pageRatio; i++) {
            final int index = i;
            final int currentPage = ((page - 1) * pageRatio) + index;
            String url = switch (searchType) {
                case BASE_URL -> urlBuilderService.baseURL(currentPage);
                case SEARCH -> urlBuilderService.search(queryString, currentPage);
                case TAG -> urlBuilderService.tag(queryString, currentPage);
                case CATEGORY -> urlBuilderService.category(queryString, currentPage);
            };
            CompletableFuture<List<ComicSearchDTO>> future = new CompletableFuture<>();

            try {
                List<ComicSearchDTO> comics =
                        getComicsScrapperService.getComicsCache(url);
                CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                List<ComicSearchDTO> comicsCache = getComicsScrapperService.getComicsCache(url);
                                comicsCache.forEach(this::setDownloadingStatus);
                                return comicsCache;
                            } catch (ComicScrapperNotInCache ex) {
                                throw new CompletionException(ex);
                            }
                        }, executor)
                        .whenComplete((resultCache, ex) -> {
                            if (ex != null) {
                                future.completeExceptionally(ex);
                            } else {
                                future.complete(resultCache);
                            }
                        });
                futures.add(future);
            } catch (ComicScrapperNotInCache exCache) {
                CompletableFuture
                        .supplyAsync(() -> {

                            rateLimiter.acquire();

                            try {
                                List<ComicSearchDTO> comics =
                                        getComicsScrapperService.getComics(url, currentPage);
                                comics.forEach(this::setDownloadingStatus);
                                return comics;
                            } catch (ComicScrapperParsingException |
                                     ComicScrapperGatewayException |
                                     ComicScrapperGatewayPageException e) {
                                ExceptionEntity exception = ExceptionEntity.builder()
                                        .timeStamp(new Date())
                                        .message(e.getMessage())
                                        .type(e.getClass().getSimpleName())
                                        .details(Arrays.stream(e.getStackTrace())
                                                .map(StackTraceElement::toString)
                                                .toList())
                                        .build();
                                exceptionService.save(exception);
                                throw new CompletionException(e);
                            }
                        }, executor)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                future.completeExceptionally(ex);
                            } else {
                                future.complete(result);
                            }
                        });
                futures.add(future);
            }
        }

        List<ComicSearchDTO> allComics = new ArrayList<>();
        boolean endReached = false;

        for (int i = 0; i < futures.size(); i++) {
            try {
                List<ComicSearchDTO> pageComics = futures.get(i).join();
                allComics.addAll(pageComics);
            } catch (CompletionException e) {
                Throwable cause = e.getCause();
                if (i == 0 && cause instanceof ComicScrapperGatewayException) {
                    throw (ComicScrapperGatewayException) cause;
                }
                endReached = true;
                break;
            }
        }
        executor.shutdown();
        return ScrapperResponseDTO.builder()
                .comicsSearchs(allComics)
                .endReached(endReached)
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/search")
    public ScrapperResponseDTO search(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "pageRatio", required = false, defaultValue = "1") int pageRatio,
            @RequestParam(name = "query", required = false, defaultValue = "") String query
    ) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {

        setBaseUrl();
        return scrappeComics(page, pageRatio, configurationService.getConfiguration().getGetComicsConfiguration().getRequestsPerSecond(), SearchType.SEARCH, query);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/tag")
    public ScrapperResponseDTO tag(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "pageRatio", required = false, defaultValue = "1") int pageRatio,
            @RequestParam(name = "tag", required = false, defaultValue = "") String tag
    ) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {

        setBaseUrl();
        return scrappeComics(page, pageRatio, configurationService.getConfiguration().getGetComicsConfiguration().getRequestsPerSecond(), SearchType.TAG, tag);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/latest")
    public ScrapperResponseDTO latest(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "pageRatio", required = false, defaultValue = "1") int pageRatio
    ) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {
        setBaseUrl();
        return scrappeComics(page, pageRatio, configurationService.getConfiguration().getGetComicsConfiguration().getRequestsPerSecond(), SearchType.BASE_URL, "nevermind");
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/category")
    public ScrapperResponseDTO category(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "pageRatio", required = false, defaultValue = "1") int pageRatio,
            @RequestParam(name = "category", required = false, defaultValue = "") String category
    ) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {
        setBaseUrl();
        return scrappeComics(page, pageRatio, configurationService.getConfiguration().getGetComicsConfiguration().getRequestsPerSecond(), SearchType.CATEGORY, category);
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
        comicSearchDto.setDownloadingStatus("not-downloaded");
        //Series are never considered as download. To be improved
        if (comicEntity.isPresent() && (comicEntity.get().getIdGcIssue() == null || comicEntity.get().getIdGcIssue().isBlank())) {
            comicSearchDto.setDownloadingStatus("downloaded");
        } else {
            for (ComicSearchDTO comic : downloadService.getListDownloadingComics()) {
                if (comic.getIdGc().equals(comicSearchDto.getIdGc())) {
                    comicSearchDto.setDownloadingStatus("downloading");
                    comicSearchDto.setTotalBytes(comic.getTotalBytes());
                    comicSearchDto.setCurrentBytes(comic.getCurrentBytes());
                    break;
                }
            }
        }
    }
}

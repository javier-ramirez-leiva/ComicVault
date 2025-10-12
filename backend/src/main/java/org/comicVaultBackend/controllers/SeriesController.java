package org.comicVaultBackend.controllers;

import org.comicVaultBackend.annotations.SkipLogging;
import org.comicVaultBackend.annotations.WithSeriesLock;
import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.ComicDTO;
import org.comicVaultBackend.domain.dto.SeriesDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.domain.entities.SeriesEntity;
import org.comicVaultBackend.exceptions.EmptySeriesException;
import org.comicVaultBackend.exceptions.EntityNotFoundException;
import org.comicVaultBackend.exceptions.FileManagerException;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.ConfigurationService;
import org.comicVaultBackend.services.FileManagerService;
import org.comicVaultBackend.services.SeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.version}/series")
public class SeriesController {
    @Autowired
    private SeriesService seriesService;

    @Autowired
    private Mapper<SeriesEntity, SeriesDTO> seriesMapper;

    @Autowired
    private Mapper<ComicEntity, ComicDTO> comicMapper;

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private FileManagerService fileManagerService;


    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping
    public List<SeriesDTO> listSeries() {
        List<SeriesEntity> comics = seriesService.listAll();
        return comics.stream()
                .map(seriesMapper::mapTo)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    /*return latest 10 comics*/
    @GetMapping(path = "/new")
    public List<SeriesDTO> listNewSeries() {
        List<SeriesEntity> series = seriesService.listAll();
        return series.stream()
                .limit(10)
                .map(seriesMapper::mapTo)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    /*return comics with page status>0 and read status==false*/
    @GetMapping(path = "/ongoing")
    public List<SeriesDTO> listOnGoingSeries() {
        List<SeriesEntity> series = seriesService.listOnGoingSeries();
        return series.stream()
                .limit(10)
                .map(seriesMapper::mapTo)
                .collect(Collectors.toList());

    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/search")
    public SeriesDTO getSeriesByID(@RequestParam(name = "id", required = false, defaultValue = "") String id) throws EntityNotFoundException {

        Optional<SeriesEntity> seriesEntity;
        if (!id.isBlank()) {
            seriesEntity = seriesService.getSeriesByID(id);
        } else {
            throw new EntityNotFoundException(id, EntityNotFoundException.Entity.SERIES);
        }
        return seriesMapper.mapTo(seriesEntity);
    }


    @GetMapping(path = "/{seriesID}/cover/small")
    @SkipLogging
    public ResponseEntity<byte[]> coverSmall(@PathVariable String seriesID) throws FileManagerException, IOException, EntityNotFoundException, EmptySeriesException {

        Optional<SeriesEntity> seriesEntityOptional = seriesService.getSeriesByID(seriesID);
        String comicID = null;
        if (seriesEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(seriesID, EntityNotFoundException.Entity.SERIES);
        }
        comicID = seriesEntityOptional.get().getComics().get(0).getId();
        if (comicID == null) {
            throw new EmptySeriesException(seriesID);
        }

        Path coverPath = fileManagerService.getSmallCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicID, true);


        File imgFile = coverPath.toFile();
        InputStream is = new FileInputStream(imgFile);
        byte[] imageBytes = StreamUtils.copyToByteArray(is);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Or use appropriate image MIME type
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @GetMapping(path = "/{seriesID}/cover/medium")
    @SkipLogging
    public ResponseEntity<byte[]> coverMedium(@PathVariable String seriesID) throws IOException, FileManagerException, EntityNotFoundException, EmptySeriesException {
        Optional<SeriesEntity> seriesEntityOptional = seriesService.getSeriesByID(seriesID);
        String comicID = null;
        if (seriesEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(seriesID, EntityNotFoundException.Entity.SERIES);
        }
        comicID = seriesEntityOptional.get().getComics().get(0).getId();
        if (comicID == null) {
            throw new EmptySeriesException(seriesID);
        }

        Path coverPath = fileManagerService.getMediumCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicID, true);

        File imgFile = coverPath.toFile();
        InputStream is = new FileInputStream(imgFile);
        byte[] imageBytes = StreamUtils.copyToByteArray(is);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Or use appropriate image MIME type
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/{seriesID}/currentComic")
    public ComicDTO currentComic(@PathVariable String seriesID) throws EntityNotFoundException, EmptySeriesException {
        Optional<SeriesEntity> seriesEntityOptional = seriesService.getSeriesByID(seriesID);
        ComicEntity comicEntity = null;
        if (seriesEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(seriesID, EntityNotFoundException.Entity.SERIES);
        }
        comicEntity = seriesEntityOptional.get().getComics().get(0);

        if (comicEntity == null) {
            throw new EmptySeriesException(seriesID);
        }
        return comicMapper.mapTo(comicEntity);
    }

    @WithSeriesLock
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR')")
    @PostMapping(path = "/{seriesID}")
    public void setComicProperties(@PathVariable String seriesID, @RequestBody SeriesDTO requestBodySeries) throws EntityNotFoundException, IllegalAccessException {
        Optional<SeriesEntity> seriesEntityOptional = seriesService.getSeriesByID(seriesID);
        if (seriesEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(seriesID, EntityNotFoundException.Entity.COMIC);
        }
        seriesService.updateNonNullProperties(requestBodySeries, seriesEntityOptional.get());
    }

}


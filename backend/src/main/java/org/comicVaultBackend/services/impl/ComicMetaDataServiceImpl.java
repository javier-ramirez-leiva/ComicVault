package org.comicVaultBackend.services.impl;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.comicVaultBackend.domain.dto.*;
import org.comicVaultBackend.domain.regular.ComicTitle;
import org.comicVaultBackend.exceptions.ComicFileException;
import org.comicVaultBackend.exceptions.ComicMetaDataException;
import org.comicVaultBackend.exceptions.ComicVineException;
import org.comicVaultBackend.services.*;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class ComicMetaDataServiceImpl implements ComicMetaDataService {

    @Autowired
    private ComicTitleParserService comicTitleParserService;

    @Autowired
    private ComicFileService comicFileService;

    @Autowired
    private ComicVineService comicVineService;

    @Autowired
    private ConfigurationService configurationService;

    private static final Logger logger = LoggerFactory.getLogger(ComicMetaDataServiceImpl.class);

    @Override
    public ComicDTO getComicMetaData(Path filePath, ComicSearchDetailsDTO comicSearchDetailsDto, String baseUrl) throws ComicMetaDataException, ComicFileException {
        if (!filePath.toFile().exists()) {
            String message = "File does not exist: " + filePath.toString();
            logger.error(message);
            throw new ComicMetaDataException(message);
        }

        double sizeMB = filePath.toFile().length() / (1024.0 * 1024.0);

        String size = String.format("%.2f MB", sizeMB);

        String fileName = _getFileNameWithoutExtension(filePath);

        ComicTitle comicTitle = comicTitleParserService.parseTitle(fileName);

        String series;

        try {
            Path parentPath = filePath.getParent();
            series = parentPath.getFileName().toString();
        } catch (Exception e) {
            series = comicTitle.getSeries();
        }

        String id;

        try {
            id = _createHasFromString(fileName);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash SHA-256 Error");
            throw new ComicMetaDataException("Hash SHA-256 Error");
        }

        String idGc = (comicSearchDetailsDto != null) ? comicSearchDetailsDto.getIdGc() : id;


        String seriesID;

        try {
            seriesID = _createHasFromString(series);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash SHA-256 Error");
            throw new ComicMetaDataException("Hash SHA-256 Error");
        }

        String category;

        try {
            Path grandparentPath = filePath.getParent().getParent();
            category = grandparentPath.getFileName().toString();
        } catch (Exception e) {
            category = "other-comics";
        }

        Integer issueNumber;

        if (comicTitle.getIssueNumber() != 0) {
            issueNumber = comicTitle.getIssueNumber();
        } else if (comicTitle.getVolumeNumber() != 0) {
            issueNumber = comicTitle.getVolumeNumber();
        } else {
            issueNumber = 1;
        }

        String year = comicSearchDetailsDto != null && comicSearchDetailsDto.getYear() != null && !comicSearchDetailsDto.getYear().isBlank() ? comicSearchDetailsDto.getYear() : "";
        String link = comicSearchDetailsDto != null && comicSearchDetailsDto.getLink() != null && !comicSearchDetailsDto.getLink().isBlank() ? comicSearchDetailsDto.getLink().replace(baseUrl, "") : "";
        String description = comicSearchDetailsDto != null && comicSearchDetailsDto.getDescription() != null && !comicSearchDetailsDto.getDescription().isBlank() ? comicSearchDetailsDto.getDescription() : "";
        List<TagDTO> tags = (comicSearchDetailsDto != null) ? comicSearchDetailsDto.getTags() : null;

        Integer pages = comicFileService.getPages(filePath);
        if (pages <= 0) {
            String message = "Pages could not be counted: " + filePath.toString();
            logger.error(message);
            throw new ComicMetaDataException(message);
        }
        List<Integer> doublePages = comicFileService.listDoublePages(filePath);
        String idgcIssue = (comicSearchDetailsDto == null || comicSearchDetailsDto.getIdGcIssue() == null || comicSearchDetailsDto.getIdGcIssue().isBlank()) ? null : comicSearchDetailsDto.getIdGcIssue();


        return ComicDTO.builder().id(id).idGc(idGc).title(fileName).issue(issueNumber).seriesID(seriesID).year(year).description(description).seriesTitle(series).
                category(category).path(filePath.toString()).link(link).pages(pages).pageStatus(0).readStatus(false).size(size).idGcIssue(idgcIssue).tags(tags).doublePages(doublePages)
                .doublePageCover(_shouldDoublePageCover(doublePages, pages))
                .build();


    }

    @Override
    public String getComicID(Path filePath) throws ComicMetaDataException {
        String fileName = _getFileNameWithoutExtension(filePath);
        try {
            return _createHasFromString(fileName);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash SHA-256 Error");
            throw new ComicMetaDataException("Hash SHA-256 Error");
        }
    }

    @Override
    public SeriesDTO getSeriesMetaData(ComicDTO comicDto) throws ComicMetaDataException {

        List<ComicDTO> comics = new ArrayList<ComicDTO>();

        return SeriesDTO.builder().id(comicDto.getSeriesID()).title(comicDto.getSeriesTitle()).category(comicDto.getCategory()).year(comicDto.getYear()).comics(comics).
                build();
    }

    @Override
    public ComicSearchDetailsDTO getMetadataFromComicPath(Path file, boolean useComicVine) throws ComicMetaDataException {
        Path comicFolder = file.getParent();
        Path gcdFilePath = Paths.get(comicFolder.toString(), "comic.gdc");
        if (Files.exists(gcdFilePath)) {
            try {
                String jsonContent = Files.readString(gcdFilePath);
                Gson gson = new Gson();

                ComicSearchDetailsDTO comicSearchDetailsDTO = gson.fromJson(jsonContent, ComicSearchDetailsDTO.class);

                /*At least get the bare minimum, the series and the category*/
                if (comicSearchDetailsDTO.getSeries() == null || comicSearchDetailsDTO.getSeries().isBlank()) {
                    comicSearchDetailsDTO.setSeries(comicFolder.getFileName().toString());
                }
                if (comicSearchDetailsDTO.getCategory() == null || comicSearchDetailsDTO.getCategory().isBlank()) {
                    String category = comicFolder.getParent().getFileName().toString();
                    comicSearchDetailsDTO.setCategory(ComicSearchDTO.getClosestCategory(category));
                }
                return comicSearchDetailsDTO;
            } catch (Exception e) {
                String message = "Error on format comic.gdc: " + gcdFilePath.toString();
                logger.error(message);
                throw new ComicMetaDataException(message, e);
            }
        } else if (useComicVine) {
            String fileName = file.getFileName().toString();
            String issueName = fileName.replaceFirst("\\.[^.]+$", "");
            try {
                Optional<ComicVineServiceImpl.ComicVineIssueInfo> optionalComicVineIssueInfo = comicVineService.searchIssue(issueName, configurationService.getConfiguration().getComicVineAPIKey());
                if (optionalComicVineIssueInfo.isPresent()) {
                    ComicSearchDetailsDTO comicSearchDetailsDTO = new ComicSearchDetailsDTO();
                    //Try to retrieve information from file path
                    boolean pathMetaFound = false;
                    Path categoryFolder = comicFolder.getParent();

                    if (categoryFolder != null
                            && Files.isDirectory(categoryFolder)
                            && Set.of("marvel", "dc", "other-comics")
                            .contains(categoryFolder.getFileName().toString().toLowerCase())) {
                        pathMetaFound = true;
                        comicSearchDetailsDTO.setCategory(categoryFolder.getFileName().toString().toLowerCase());
                        comicSearchDetailsDTO.setSeries(comicFolder.getFileName().toString());
                    }

                    ComicVineServiceImpl.ComicVineIssueInfo comicVineIssueInfo = optionalComicVineIssueInfo.get();
                    if (!pathMetaFound && StringUtils.isNotBlank(comicVineIssueInfo.category())) {
                        comicSearchDetailsDTO.setCategory(comicVineIssueInfo.category());
                    }
                    if (StringUtils.isNotBlank(comicVineIssueInfo.description())) {
                        String description = Jsoup.parse(comicVineIssueInfo.description()).text();
                        comicSearchDetailsDTO.setDescription(description);
                    }
                    if (!pathMetaFound && StringUtils.isNotBlank(comicVineIssueInfo.series())) {
                        comicSearchDetailsDTO.setSeries(comicVineIssueInfo.series());
                    }
                    if (comicVineIssueInfo.year() != null) {
                        comicSearchDetailsDTO.setYear((comicVineIssueInfo.year().toString()));
                    }
                    return comicSearchDetailsDTO;
                } else {
                    //Generate a ComicSearchDetailsDto with only the ID
                    String message = "No ComicVine metadata for file: " + file.toString();
                    logger.info(message);
                    throw new ComicMetaDataException(message);
                }
            } catch (ComicVineException e) {
                logger.error(e.getMessage());
                throw new ComicMetaDataException(e.getMessage());
            }

        } else {
            //Generate a ComicSearchDetailsDto with only the ID
            String message = "No comic.gdc or ComicVine metadata for file: " + file.toString();
            logger.info(message);
            throw new ComicMetaDataException(message);
        }
    }


    private String _createHasFromString(String inputString) throws NoSuchAlgorithmException {
        byte[] encodedBytes = inputString.getBytes();

        // Create a SHA-256 hash object
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Update the hash object with the encoded bytes
        byte[] hashBytes = digest.digest(encodedBytes);

        // Encode the digest using Base64
        String base64Encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);

        // Truncate to the first 10 characters
        return base64Encoded.length() > 10 ? base64Encoded.substring(0, 10) : base64Encoded;
    }

    private String _getFileNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString();

        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex == -1) {
            return fileName;
        }

        return fileName.substring(0, lastDotIndex);
    }

    public boolean _shouldDoublePageCover(List<Integer> doublePages, int pages) {

        //If there are double pages, if the first one is even cover should double
        if (!doublePages.isEmpty()) {
            int firstDoublePage = doublePages.get(0);
            return firstDoublePage % 2 == 0;
        }

        // If there are no double pages, if the pages number is even cover should double
        return pages % 2 == 0;
    }
}

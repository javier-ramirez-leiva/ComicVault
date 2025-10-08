package org.comicVaultBackend.services.impl;

import com.google.gson.Gson;
import org.comicVaultBackend.domain.dto.*;
import org.comicVaultBackend.domain.regular.ComicTitle;
import org.comicVaultBackend.exceptions.ComicFileException;
import org.comicVaultBackend.exceptions.ComicMetaDataException;
import org.comicVaultBackend.services.ComicFileService;
import org.comicVaultBackend.services.ComicMetaDataService;
import org.comicVaultBackend.services.ComicTitleParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ComicMetaDataServiceImpl implements ComicMetaDataService {

    @Autowired
    private ComicTitleParserService comicTitleParserService;

    @Autowired
    private ComicFileService comicFileService;

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
    public ComicSearchDetailsDTO getMetadataFromComicPath(Path file) throws ComicMetaDataException {
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
        } else {
            //Generate a ComicSearchDetailsDto with only the ID
            String message = "No comic.gdc for file: " + file.toString();
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

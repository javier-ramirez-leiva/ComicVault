package org.comicVaultBackend.controllers;


import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.comicVaultBackend.annotations.SkipLogging;
import org.comicVaultBackend.annotations.SkipLoggingResponseBody;
import org.comicVaultBackend.annotations.WithComicLock;
import org.comicVaultBackend.annotations.WithUserLock;
import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.*;
import org.comicVaultBackend.domain.entities.*;
import org.comicVaultBackend.domain.regular.ComicTitle;
import org.comicVaultBackend.domain.regular.DeleteReadOptions;
import org.comicVaultBackend.exceptions.*;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.version}")
public class ComicController {
    @Autowired
    private ComicService comicService;

    @Autowired
    private Mapper<ComicEntity, ComicDTO> comicMapper;

    @Autowired
    private Mapper<SeriesEntity, SeriesDTO> seriesMapper;

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ComicMetaDataService comicMetaDataService;

    @Autowired
    private SeriesService seriesService;

    @Autowired
    private ComicFileService comicFileService;

    @Autowired
    private ComicTitleParserService comicTitleParserService;

    @Autowired
    private GetComicsScrapperService comicsScrapperService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private SlackNotifyService slackNotifyService;

    @Autowired
    private LogService logService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private Mapper<LogEntity, LogDTO> logMapper;

    @Autowired
    private FileManagerService fileManagerService;

    @Autowired
    private TagService tagService;

    @Autowired
    private Mapper<TagEntity, TagDTO> tagMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private JobService jobService;

    private static final Logger logger = LoggerFactory.getLogger(ComicController.class);

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "downloads")
    public List<ComicSearchDTO> downloads() {
        return downloadService.getListDownloadingComics();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "comics")
    public List<ComicDTO> listComics() {

        List<ComicEntity> comics = comicService.listAll();

        return comics.stream()
                .map(comicMapper::mapTo)
                .collect(Collectors.toList());
    }

    /*return latest 10 comics*/
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/comics/new")
    public List<ComicDTO> listNewComics() {
        List<ComicEntity> comics = comicService.listAll();
        return comics.stream()
                .limit(10)
                .map(comicMapper::mapTo)
                .collect(Collectors.toList());
    }

    /*return comics with page status>0 and read status==false*/
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/comics/ongoing")
    public List<ComicDTO> listOnGoingComics() {
        List<ComicEntity> comics = comicService.listOnGoing();
        return comics.stream()
                .map(comicMapper::mapTo)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR')")
    @PostMapping(path = "/scanLib")
    public void scanLib() throws FileManagerException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        JobDTO jobDTO = JobDTO.builder().username(username).timeStamp(new Date()).type(JobEntity.Type.SCAN_LIB).status(JobEntity.STATUS.ON_GOING).build();
        JobEntity jobEntity = jobService.createJob(jobDTO);

        try {
            SCAN_LIB(jobEntity);
            jobService.finishJob(jobEntity, JobEntity.STATUS.COMPLETED);
        } catch (FileManagerException e) {
            jobService.finishJob(jobEntity, JobEntity.STATUS.ERROR);
            logger.error("Error scanning lib : {}", e.getMessage());
        }

    }

    private void SCAN_LIB(JobEntity jobEntity) throws FileManagerException {
        String downloadRoot = configurationService.getConfiguration().getDownloadRoot();
        Path downloadRootPath = Paths.get(downloadRoot);
        if (configurationService.getConfiguration().isScanArchives()) {
            //Try to unzip rar files inside, as they may contain comics
            List<Path> archiveFiles = fileManagerService.findFilesWithExtensions(downloadRootPath, ".zip", ".rar");
            for (Path archiveFile : archiveFiles) {
                try {
                    comicFileService.extract(archiveFile, configurationService.getConfiguration().isDeleteArchives());
                } catch (Exception e) {
                    logger.error("Error extracting archive '{}': {}", archiveFile.toString(), e.getMessage());
                    LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity("Error").severityMessage("Extract Error").username(jobEntity.getUsername()).message(archiveFile.toString()).details(e.getMessage()).build();
                    addLog(logDTO, jobEntity);
                }
            }
        }

        // Get a list of files with .cbr or .cbz extension
        List<Path> files = fileManagerService.findFilesWithExtensions(downloadRootPath, ".cbr", ".cbz");
        // Order files to manage by file name, it could help if the comic could not be parsed
        files.sort(Comparator.comparing(Path::toString, String.CASE_INSENSITIVE_ORDER));
        for (Path file : files) {
            try {
                String id = comicMetaDataService.getComicID(file);
                if (comicService.getComicByID(id).isEmpty()) {
                    ComicSearchDetailsDTO comicSearchDetailsDto;
                    try {
                        comicSearchDetailsDto = comicMetaDataService.getMetadataFromComicPath(file);
                    } catch (ComicMetaDataException e) {
                        //Normally from this part, we check nullity of the comicSearchDetailsDto
                        comicSearchDetailsDto = null;
                    }
                    addComicToDataBaseComplete(file, jobEntity, comicSearchDetailsDto);
                }
                //TEMP FIX
                /*
                Path comicFilePath = Paths.get(comicEntity.getPath());
                List<Integer> listDoublePages = comicFileService.listDoublePages(comicFilePath);
                comicEntity.setDoublePages(listDoublePages);


                boolean doublePageCover;
                List<Integer> doublePages = comicEntity.getDoublePages();

                //If there are double pages, if the first one is even cover should double
                if (!doublePages.isEmpty()) {
                    int firstDoublePage = doublePages.get(0);
                    doublePageCover = firstDoublePage % 2 == 0;
                } else {
                    // If there are no double pages, if the pages number is even cover should double
                    doublePageCover = comicEntity.getPages() % 2 == 0;
                }
                comicEntity.setDoublePageCover(doublePageCover);

                comicService.save(comicEntity);
                */
                //TEMP FIX
            } catch (Exception e) {
                logger.error("Error adding comic to database '{}': {}", file.toString(), e.getMessage());
                LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity("Error").severityMessage("Add Error").username(jobEntity.getUsername()).message(file.toString()).details(e.getMessage()).build();
                addLog(logDTO, jobEntity);
            }
        }

        //Speed up the process with up to 5 threads in parallel
        if (configurationService.getConfiguration().isGenerateNavigationThumbnails()) {
            for (ComicEntity comicEntity : comicService.listAll()) {
                Path miniPagesFolder = fileManagerService.getMiniPagesFolderPath(configurationService.getConfiguration().getDownloadRoot(), comicEntity.getId(), false, false);
                if (!Files.exists(miniPagesFolder) || !Files.isDirectory(miniPagesFolder)) {
                    Path comicFile = Paths.get(comicEntity.getPath());
                    try {
                        Path miniPagesFolderGenerate = fileManagerService.getMiniPagesFolderPath(configurationService.getConfiguration().getDownloadRoot(), comicEntity.getId(), false, true);
                        record ComicJobArgs(Path comicFile, Path miniPagesFolder, int resolution) {
                        }
                        ;
                        schedulerService.schedule(args -> {
                            try {
                                comicFileService.savePagesToFolder(args.comicFile(), args.miniPagesFolder(), args.resolution());
                            } catch (ComicFileException e) {
                                logger.error("Error generating mini pages '{}': {}", args.comicFile(), e.getMessage());
                            }
                        }, new ComicJobArgs(comicFile, miniPagesFolderGenerate, 200));
                    } catch (Exception e) {
                        logger.error("Error generating mini pages '{}': {}", comicFile.toString(), e.getMessage());
                    }

                }
            }
        }

        if (configurationService.getConfiguration().getSlackConfiguration().isEnableNotifications()) {
            try {
                slackNotifyService.sendNotification("Scan", configurationService.getNotificationFile(), configurationService.getConfiguration().getSlackConfiguration().getSlackWebHook(), configurationService.getConfiguration().getSlackConfiguration().getComicVaultBaseUrl(), null, null);
            } catch (SlackNotifyException e) {
                logger.error("Error sending slack notification :{}", e.getMessage());
            }
        }
    }

    @GetMapping(path = "/comics/{comicID}/cover/small")
    @SkipLogging
    public ResponseEntity<byte[]> smallCover(@PathVariable String comicID) throws FileManagerException, IOException {
        Path coverPath = fileManagerService.getSmallCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicID, true);

        File imgFile = coverPath.toFile();
        InputStream is = new FileInputStream(imgFile);
        byte[] imageBytes = StreamUtils.copyToByteArray(is);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Or use appropriate image MIME type
        headers.setContentLength(imageBytes.length);

        // Prevent caching old images
        headers.setLastModified(imgFile.lastModified());

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

    }

    @GetMapping(path = "/comics/{comicID}/cover/medium")
    @SkipLogging
    public ResponseEntity<byte[]> mediumCover(@PathVariable String comicID) throws FileManagerException, IOException {
        Path coverPath = fileManagerService.getMediumCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicID, true);

        File imgFile = coverPath.toFile();
        InputStream is = new FileInputStream(imgFile);
        byte[] imageBytes = StreamUtils.copyToByteArray(is);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Or use appropriate image MIME type
        headers.setContentLength(imageBytes.length);

        // Prevent caching old images
        headers.setLastModified(imgFile.lastModified());

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @WithComicLock
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER')")
    @PostMapping(path = "/comics/{comicID}/setCover")
    @SkipLogging
    public void setCover(@PathVariable String comicID, @RequestBody int page) throws Exception {
        Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);
        if (comicEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(comicID, EntityNotFoundException.Entity.COMIC);
        }
        Path comicFile = Paths.get(comicEntityOptional.get().getPath());
        fileManagerService.getSmallCoverPath(configurationService.getConfiguration().getDownloadRoot(), true);
        fileManagerService.getMediumCoverPath(configurationService.getConfiguration().getDownloadRoot(), true);
        Path coverPathSmall = fileManagerService.getSmallCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicEntityOptional.get().getId(), false);
        Path coverPathMedium = fileManagerService.getMediumCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicEntityOptional.get().getId(), false);


        comicFileService.savePageToFile(comicFile, coverPathSmall, page, 300);
        comicFileService.savePageToFile(comicFile, coverPathMedium, page, 600);

        return;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/comics/search")
    public ComicDTO getComicByID(@RequestParam(name = "id", required = false, defaultValue = "") String id, @RequestParam(name = "idGc", required = false, defaultValue = "") String idGc) throws EntityNotFoundException, IllegalArgumentException {

        Optional<ComicEntity> comicEntity;
        if (!id.isBlank()) {
            comicEntity = comicService.getComicByID(id);
        } else if (!idGc.isBlank()) {
            comicEntity = comicService.getcomicbyidGc(idGc);
        } else {
            throw new IllegalArgumentException();
        }

        if (comicEntity.isEmpty()) {
            String idEntity = id.isBlank() ? idGc : id;
            throw new EntityNotFoundException(idEntity, EntityNotFoundException.Entity.COMIC);
        }

        return comicMapper.mapTo(comicEntity.get());
    }

    @WithComicLock
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR')")
    @PostMapping(path = "/comics/{comicID}")
    public void setComicProperties(@PathVariable String comicID, @RequestBody ComicDTO requestBodyComic) throws EntityNotFoundException, IllegalAccessException, EntityWriteException {
        Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);
        if (comicEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(comicID, EntityNotFoundException.Entity.COMIC);
        }
        comicService.updateNonNullProperties(requestBodyComic, comicEntityOptional.get());
    }

    @WithUserLock
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @PostMapping(path = "/comicList/markAsRead")
    public void markAsRead(@RequestBody List<String> comicIDs) {
        markAsReadValue(comicIDs, true);
    }

    @WithUserLock
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @PostMapping(path = "/comicList/markAsNotRead")
    public void markAsNotRead(@RequestBody List<String> comicIDs) {
        markAsReadValue(comicIDs, false);
    }

    private void markAsReadValue(List<String> comicIDs, boolean value) {
        List<ComicEntity> listComics = new ArrayList<>();
        for (String comicID : comicIDs) {
            Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);
            if (comicEntityOptional.isEmpty()) {
                logger.error("comicID not found: {}", comicID);
            } else {
                listComics.add(comicEntityOptional.get());
            }
        }
        for (ComicEntity comicEntity : listComics) {
            comicService.setReadStatus(comicEntity, value);

        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER','CONTRIBUTOR')")
    @PostMapping(path = "/comicList/setIssues")
    public void setIssueNumbers(@RequestBody Map<String, Integer> comicIdsIssues) {
        //This method is unsafe, so make sure that there are no duplicates of number issues inside the same series
        // Iterate over each key-value pair in the map
        for (Map.Entry<String, Integer> entry : comicIdsIssues.entrySet()) {
            String comicId = entry.getKey();
            Integer issueNumber = entry.getValue();

            Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicId);

            if (comicEntityOptional.isEmpty()) {
                logger.error("comicID not found: {}", comicId);
                continue;
            }
            comicEntityOptional.get().setIssue(issueNumber);
            comicService.save(comicEntityOptional.get());
        }
    }

    @WithComicLock
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER')")
    @DeleteMapping(path = "/comics/{comicID}")
    public ResponseEntity<Map<String, String>> deleteComic(@PathVariable String comicID) throws EntityNotFoundException, FileManagerException {

        Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (comicEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(comicID, EntityNotFoundException.Entity.COMIC);
        }
        JobDTO jobDTO = JobDTO.builder().username(username).timeStamp(new Date()).type(JobEntity.Type.DELETE).status(JobEntity.STATUS.ON_GOING).build();
        JobEntity jobEntity = jobService.createJob(jobDTO);
        try {
            String nextComicID = deleteComic(comicEntityOptional.get(), true, jobEntity);
            jobService.finishJob(jobEntity, JobEntity.STATUS.COMPLETED);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("nextComicID", nextComicID);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (FileManagerException e) {
            jobService.finishJob(jobEntity, JobEntity.STATUS.ERROR);
            throw e;
        }
    }

    //Post verb because the body may be too large for an url
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER')")
    @PostMapping(path = "/comicList/delete")
    public void deleteComicList(@RequestBody List<String> comicIDs) {

        List<ComicEntity> listComics = new ArrayList<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        for (String comicID : comicIDs) {
            Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);
            if (comicEntityOptional.isEmpty()) {
                logger.error("comicID not found: {}", comicID);
            } else {
                listComics.add(comicEntityOptional.get());
            }
        }

        //Delete list is always good even if there is some error in here
        JobDTO jobDTO = JobDTO.builder().username(username).timeStamp(new Date()).type(JobEntity.Type.DELETE).status(JobEntity.STATUS.ON_GOING).build();
        JobEntity jobEntity = jobService.createJob(jobDTO);
        for (ComicEntity comicEntity : listComics) {
            try {
                deleteComic(comicEntity, true, jobEntity);
            } catch (FileManagerException ex) {
                logger.error(ex.getMessage());
            }
        }
        jobService.finishJob(jobEntity, JobEntity.STATUS.COMPLETED);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER')")
    @DeleteMapping(path = "/comics/clean")
    public void cleanLibrary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        JobDTO jobDTO = JobDTO.builder().username(username).timeStamp(new Date()).type(JobEntity.Type.CLEAN_LIB).status(JobEntity.STATUS.ON_GOING).build();
        JobEntity jobEntity = jobService.createJob(jobDTO);

        CLEAN_LIB(jobEntity);
        jobService.finishJob(jobEntity, JobEntity.STATUS.COMPLETED);
    }

    private void CLEAN_LIB(JobEntity jobEntity) {
        List<ComicEntity> listComics = comicService.listAll();

        for (ComicEntity comic : listComics) {
            String comicPath = comic.getPath();
            Path path = Paths.get(comicPath);
            if (Files.notExists(path)) {
                try {
                    deleteComic(comic, false, jobEntity);
                } catch (FileManagerException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }

        /*Clean covers*/
        try {
            Path smallCovers = fileManagerService.getSmallCoverPath(configurationService.getConfiguration().getDownloadRoot(), false);
            List<Path> coverFiles = fileManagerService.findFilesWithExtensions(smallCovers, ".jpg");
            for (Path coverFile : coverFiles) {
                String comicId = coverFile.getFileName().toString().replace(".jpg", "");
                if (comicService.getComicByID(comicId).isEmpty()) {
                    try {
                        fileManagerService.deleteFile(coverFile);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                    }
                }

            }
        } catch (Exception e) {
            //No small covers, no problem
        }

        try {
            Path mediumCovers = fileManagerService.getMediumCoverPath(configurationService.getConfiguration().getDownloadRoot(), false);
            List<Path> coverFiles = fileManagerService.findFilesWithExtensions(mediumCovers, ".jpg");
            for (Path coverFile : coverFiles) {
                String comicId = coverFile.getFileName().toString().replace(".jpg", "");
                if (comicService.getComicByID(comicId).isEmpty()) {
                    try {
                        fileManagerService.deleteFile(coverFile);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                    }
                }

            }
        } catch (Exception e) {
            //No small covers, no problem
        }

        try {
            Path miniPagesPath = fileManagerService.getMiniPagesPath(configurationService.getConfiguration().getDownloadRoot(), false);
            List<Path> miniPagesFolders = fileManagerService.listDirectFolders(miniPagesPath);
            for (Path miniPageFolder : miniPagesFolders) {
                String comicId = miniPageFolder.getFileName().toString();
                if (comicService.getComicByID(comicId).isEmpty()) {
                    try {
                        fileManagerService.deleteFolder(miniPageFolder);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                    }
                }

            }
        } catch (Exception e) {
            //No mini pages covers, no problem
        }

    }


    @WithUserLock
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/comics/deleteRead")
    public void deleteReadComics(@RequestBody String deleteReadOption) {
        DeleteReadOptions option;

        try {
            option = DeleteReadOptions.valueOf(deleteReadOption);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid deleteReadOption: " + deleteReadOption);
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        JobDTO jobDTO = JobDTO.builder().username(username).timeStamp(new Date()).type(JobEntity.Type.DELETE_LIB).status(JobEntity.STATUS.ON_GOING).build();
        JobEntity jobEntity = jobService.createJob(jobDTO);
        record ComicJobArgs(JobEntity jobEntity, DeleteReadOptions option) {
        }
        ;//Delete lib is complete even if there are some errors
        DELETE_LIB(jobEntity, option);
        jobService.finishJob(jobEntity, JobEntity.STATUS.COMPLETED);

    }

    private void DELETE_LIB(JobEntity jobEntity, DeleteReadOptions option) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ComicEntity> listComics = comicService.listAll();

        for (ComicEntity comic : listComics) {
            ComicDTO comicDto = comicMapper.mapTo(comic);
            boolean toDelete = false;
            if (option == DeleteReadOptions.READ_BY_ME) {
                toDelete = comicDto.getReadStatus();
            } else if (option == DeleteReadOptions.READ_BY_ME_NOT_STARTED_BY_OTHER) {
                comicDto.getProgress();
                boolean readByMe = false;
                boolean startedByOther = false;
                for (ProgressDTO progressDTO : comicDto.getProgress()) {
                    if (progressDTO.getReadStatus() && progressDTO.getUsername().equals(username)) {
                        readByMe = true;
                    }
                    if (!progressDTO.getReadStatus() && progressDTO.getPageStatus() > 0) {
                        startedByOther = true;
                    }
                }
                toDelete = readByMe & !startedByOther;
            } else if (option == DeleteReadOptions.READ_BY_ALL) {
                List<String> userNames = userService.listAll().stream()
                        .map(UserEntity::getUsername)
                        .toList();
                if (userNames.size() == comicDto.getProgress().size()) {
                    boolean allRead = true;
                    for (ProgressDTO progressDTO : comicDto.getProgress()) {
                        if (!progressDTO.getReadStatus()) {
                            allRead = false;
                            break;
                        }
                    }
                    toDelete = allRead;
                }
            }
            if (toDelete) {
                try {
                    deleteComic(comic, true, jobEntity);
                } catch (FileManagerException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
    }

    private String deleteComic(ComicEntity comicEntity, boolean deleteFiles, JobEntity jobEntity) throws FileManagerException {

        String comicPath = comicEntity.getPath();
        Path path = Paths.get(comicPath);
        Path parentPath = path.getParent();
        final String comicId = comicEntity.getId();
        String nextComicID = "";

        if (deleteFiles) {
            fileManagerService.deleteFile(path);

            try {
                Path coverPathMedium = fileManagerService.getMediumCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicEntity.getId(), true);
                fileManagerService.deleteFile(coverPathMedium);
            } catch (FileManagerException e) {
                logger.error("Cover medium could not be deleted for comic: {}", comicEntity.getId());
            }

            try {
                Path coverPathSmall = fileManagerService.getSmallCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicEntity.getId(), true);
                fileManagerService.deleteFile(coverPathSmall);
            } catch (FileManagerException e) {
                logger.error("Cover small could not be deleted for comic: {}", comicEntity.getId());
            }

            try {
                Path miniPagesFolderPath = fileManagerService.getMiniPagesFolderPath(configurationService.getConfiguration().getDownloadRoot(), comicEntity.getId(), true, false);
                fileManagerService.deleteFolder(miniPagesFolderPath);
            } catch (FileManagerException e) {
                logger.error("Mini pages could not be deleted for comic: {}", comicEntity.getId());
            }
        }

        SeriesEntity seriesEntity = comicEntity.getSeries();
        final String seriesId = seriesEntity.getSeriesID();
        int comicIndex = seriesEntity.getComics().indexOf(comicEntity);
        if (seriesEntity.getComics().size() > 1) {
            if (seriesEntity.getComics().size() == comicIndex + 1) {
                nextComicID = seriesEntity.getComics().get(comicIndex - 1).getId();
            } else {
                nextComicID = seriesEntity.getComics().get(comicIndex + 1).getId();
            }
        }

        for (ProgressEntity progressEntity : comicEntity.getProgress()) {
            progressService.delete(progressEntity);
        }
        seriesEntity.getComics().remove(comicEntity);
        seriesService.save(seriesEntity);
        comicService.deleteComicById(comicEntity.getId());


        this.logService.removeComicIdFromLog(comicId);
        LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity("Warning").severityMessage("Delete Comic").username(jobEntity.getUsername()).message(comicEntity.getTitle()).build();
        addLog(logDTO, jobEntity);

        if (seriesEntity.getComics().isEmpty()) {
            seriesService.deleteSeriesById(seriesEntity.getSeriesID());
            this.logService.removeSeriesIdFromLog(seriesId);
            LogDTO logDTOSeries = LogDTO.builder().timeStamp(new Date()).severity("Warning").severityMessage("Delete Series").username(jobEntity.getUsername()).message(seriesEntity.getTitle()).build();
            addLog(logDTOSeries, jobEntity);
            if (deleteFiles) {
                try {
                    fileManagerService.deleteFile(parentPath);
                } catch (FileManagerException e) {
                    logger.error("Could not delete folder: {}", parentPath.toString());
                }
            }
        }
        return nextComicID;
    }

    @GetMapping("/comics/{comicID}/pages/{page_num}")
    @SkipLoggingResponseBody
    public ResponseEntity<InputStreamResource> page(@PathVariable String comicID, @PathVariable Integer page_num) throws Exception {
        Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);

        if (comicEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(comicID, EntityNotFoundException.Entity.COMIC);
        }

        Path comicPath = Paths.get(comicEntityOptional.get().getPath());

        if (Files.notExists(comicPath)) {
            throw new ComicFileException("Comic file does not exist:" + comicID);
        }

        try {
            ByteArrayOutputStream outputStream = comicFileService.getPage(comicPath, page_num);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=page_" + page_num + ".jpg");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            String message = "Error getting page from file file: " + comicPath + " -> " + page_num;

            logger.error(message);
            throw new Exception(message);
        }
    }

    @GetMapping("/comics/{comicID}/doublePages/{page_num}")
    @SkipLoggingResponseBody
    public ResponseEntity<InputStreamResource> doublePage(@PathVariable String comicID, @PathVariable Integer page_num) throws Exception {
        Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);

        if (comicEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(comicID, EntityNotFoundException.Entity.COMIC);
        }

        Path comicPath = Paths.get(comicEntityOptional.get().getPath());

        if (Files.notExists(comicPath)) {
            throw new ComicFileException("Comic file does not exist:" + comicID);
        }

        try {
            ByteArrayOutputStream outputStream = null;
            List<Integer> pagesPair = comicEntityOptional.get().getPairPages(page_num);
            if (pagesPair.size() > 1) {
                outputStream = comicFileService.getDoublePage(comicPath, pagesPair.get(0));
            } else {
                outputStream = comicFileService.getPage(comicPath, page_num);
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=page_" + page_num + ".jpg");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            String message = "Error getting page from file file: " + comicPath + " -> " + page_num;

            logger.error(message);
            throw new Exception(message);
        }
    }

    @WithComicLock
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @PostMapping("/comics/{comicID}/pageStatus")
    @SkipLoggingResponseBody
    public void setPageStatus(@PathVariable String comicID, @RequestBody int page) throws Exception {
        Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);

        if (comicEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(comicID, EntityNotFoundException.Entity.COMIC);
        }
        comicService.setPageStatus(comicEntityOptional.get(), page);

        comicService.save(comicEntityOptional.get());
    }


    @GetMapping("/comics/{comicID}/minipages/{page_num}")
    @SkipLoggingResponseBody
    public ResponseEntity<byte[]> miniPage(@PathVariable String comicID, @PathVariable Integer page_num) throws Exception {
        Path miniPagesFolderPath = fileManagerService.getMiniPagesFolderPath(configurationService.getConfiguration().getDownloadRoot(), comicID, true, false);
        Path miniPagePath = Paths.get(miniPagesFolderPath.toString(), String.valueOf(page_num) + ".jpg");

        File imgFile = miniPagePath.toFile();
        InputStream is = new FileInputStream(imgFile);
        byte[] imageBytes = StreamUtils.copyToByteArray(is);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Or use appropriate image MIME type
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR')")
    @PostMapping("/download")
    @SkipLoggingResponseBody
    public void download(@RequestBody DownloadRequestDTO requestBodyDownload) throws EntityAlreadyExistringException, ConfigurationValueException {
        boolean comicExists = false;
        if (downloadService.getListDownloadingComics().stream().anyMatch(comic -> comic.getIdGc().equals(requestBodyDownload.getComicSearchDetails().getIdGc()))) {
            comicExists = true;
        } else {
            comicExists = comicService.getcomicbyidGc(requestBodyDownload.getComicSearchDetails().getIdGc()).isPresent();
        }
        if (comicExists) {
            throw new EntityAlreadyExistringException(requestBodyDownload.getComicSearchDetails().getIdGc(), EntityNotFoundException.Entity.COMIC);
        }

        if (configurationService.getConfiguration().getDownloadRoot().isBlank()) {
            throw new ConfigurationValueException("Configuration value 'downloadRoot' is empty");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        JobDTO jobDTO = JobDTO.builder().username(username).timeStamp(new Date()).type(JobEntity.Type.DOWNLOAD).status(JobEntity.STATUS.ON_GOING).build();
        JobEntity jobEntity = jobService.createJob(jobDTO);
        try {
            DOWNLOAD(jobEntity, requestBodyDownload);
            jobService.finishJob(jobEntity, JobEntity.STATUS.COMPLETED);
        } catch (Exception e) {
            jobService.finishJob(jobEntity, JobEntity.STATUS.ERROR);
        }
    }

    private void DOWNLOAD(JobEntity jobEntity, DownloadRequestDTO requestBodyDownload) throws Exception {
        //Unique download, set currentComics and totalComics manually
        requestBodyDownload.getComicSearchDetails().setCurrentComic(0);
        requestBodyDownload.getComicSearchDetails().setTotalComics(1);

        boolean success = true;
        ComicEntity newComic = null;
        try {
            newComic = downloadIssue(requestBodyDownload.getComicSearchDetails(), requestBodyDownload.getDownloadLink(), jobEntity, null);
            downloadService.removeComicFromDownloadList(requestBodyDownload.getComicSearchDetails());
            slackNotify(requestBodyDownload.getComicSearchDetails(), newComic, success);
        } catch (Exception e) {
            logger.error(e.getMessage());
            LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity("Error").severityMessage("Download Error").username(jobEntity.getUsername()).message(requestBodyDownload.getComicSearchDetails().getLink()).messageHref(true).details(e.getMessage()).build();
            addLog(logDTO, jobEntity);
            success = false;
            downloadService.removeComicFromDownloadList(requestBodyDownload.getComicSearchDetails());
            slackNotify(requestBodyDownload.getComicSearchDetails(), newComic, success);
            throw e;
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR')")
    @PostMapping("/downloadList")
    @SkipLoggingResponseBody
    public void downloadList(@RequestBody DownloadRequestListDTO downloadRequestListDTO) throws BadRequestException, ConfigurationValueException {
        boolean comicExists = false;

        if (downloadService.getListDownloadingComics().stream().anyMatch(comic -> comic.getIdGc().equals(downloadRequestListDTO.getComicSearchDetails().getIdGc()))) {
            comicExists = true;
        }
        if (comicExists) {
            throw new BadRequestException("Comic already exists");
        }

        if (configurationService.getConfiguration().getDownloadRoot().isBlank()) {
            throw new ConfigurationValueException("No storage path set");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        JobDTO jobDTO = JobDTO.builder().username(username).timeStamp(new Date()).type(JobEntity.Type.DOWNLOAD_LIST).status(JobEntity.STATUS.ON_GOING).build();
        JobEntity jobEntity = jobService.createJob(jobDTO);

        record ComicJobArgs(JobEntity jobEntity, DownloadRequestListDTO downloadRequestListDTO) {
        }
        ;
        //Downloading a list never returns an error
        DOWNLOAD_LIST(jobEntity, downloadRequestListDTO);
        jobService.finishJob(jobEntity, JobEntity.STATUS.COMPLETED);
    }

    private void DOWNLOAD_LIST(JobEntity jobEntity, DownloadRequestListDTO downloadRequestListDTO) {
        boolean success = true;
        ComicEntity newComic = null;

        //Download list, set currentComics and totalComics manually
        downloadRequestListDTO.getComicSearchDetails().setCurrentComic(0);
        downloadRequestListDTO.getComicSearchDetails().setTotalComics(downloadRequestListDTO.downloadRequests.size());

        for (DownloadIssueRequestDTO downloadRequest : downloadRequestListDTO.downloadRequests) {
            try {
                ComicEntity thisComic = downloadIssue(downloadRequestListDTO.getComicSearchDetails(), downloadRequest.getLink(), jobEntity, downloadRequest);
                if (newComic == null) {
                    newComic = thisComic;
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity("Error").severityMessage("Download Error").username(jobEntity.getUsername()).message(downloadRequestListDTO.getComicSearchDetails().getLink()).messageHref(true).details(e.getMessage()).build();
                addLog(logDTO, jobEntity);
                success = false;
            }
        }

        downloadService.removeComicFromDownloadList(downloadRequestListDTO.getComicSearchDetails());
        slackNotify(downloadRequestListDTO.getComicSearchDetails(), newComic, success);
    }

    private ComicEntity downloadIssue(ComicSearchDetailsDTO comicSearchDetailsDto, DownloadLinkDTO downloadLinkDTO, JobEntity jobEntity, @Nullable DownloadIssueRequestDTO downloadIssueRequestDTO) throws ComicScrapperParsingException, ComicScrapperGatewayException, FileManagerException, DownloadException, ComicMetaDataException, ComicFileException {
        if (downloadIssueRequestDTO != null) {
            comicSearchDetailsDto.setIdGcIssue(downloadIssueRequestDTO.getIdGcIssue());
        }

        //Try to get description if not provided
        String description = comicSearchDetailsDto.getDescription();
        if (description.isBlank()) {
            ComicSearchDetailsLinksDTO comicDetailsDto;
            comicDetailsDto = comicsScrapperService.getComicDetails(comicSearchDetailsDto.getLink());
            description = comicDetailsDto.getDescription();
            comicSearchDetailsDto.setDescription(description);
        }

        Path downloadedPath = null;
        Path comicParentPath = null;

        comicParentPath = fileManagerService.getComicPathParentFolder(configurationService.getConfiguration().getDownloadRoot(), comicSearchDetailsDto.getCategory(), comicSearchDetailsDto.getSeries(), true);


        ComicTitle comicTitle;
        if (downloadIssueRequestDTO == null) {
            comicTitle = comicTitleParserService.parseTitle(comicSearchDetailsDto.getTitle());
        } else {
            comicTitle = comicTitleParserService.parseIssueTitle(downloadIssueRequestDTO.getTitle());
        }
        Path downloadRoot = Paths.get(configurationService.getConfiguration().getDownloadRoot());
        downloadedPath = downloadService.downloadFile(downloadRoot, comicParentPath, downloadLinkDTO, comicTitle.getFileName(), comicSearchDetailsDto, comicSearchDetailsDto.getDescription(), configurationService.getConfiguration().getJDownloaderConfiguration());

        if (Files.notExists(downloadedPath)) {
            throw new DownloadException(downloadIssueRequestDTO.getTitle());
        }

        List<String> comicExtensions = new ArrayList<>(List.of(".cbr", ".cbz"));
        List<String> archiveExtensions = new ArrayList<>(List.of(".zip", ".rar"));
        ComicEntity newComic = null;
        if (comicExtensions.stream().anyMatch(downloadedPath.toString()::contains)) {
            newComic = addComicToDataBaseComplete(downloadedPath, jobEntity, comicSearchDetailsDto);
        } else if (archiveExtensions.stream().anyMatch(downloadedPath.toString()::contains)) {
            List<Path> listComicPaths = comicFileService.extract(downloadedPath, configurationService.getConfiguration().isDeleteArchives());
            for (Path comicFile : listComicPaths) {
                try {
                    if (newComic != null) {
                        addComicToDataBaseComplete(comicFile, jobEntity, comicSearchDetailsDto);
                    } else {
                        newComic = addComicToDataBaseComplete(comicFile, jobEntity, comicSearchDetailsDto);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity("Error").severityMessage("Add Comic Error").username(jobEntity.getUsername()).message(comicFile.toString()).messageHref(false).details(e.getMessage()).build();
                    addLog(logDTO, jobEntity);
                }
            }
        } else {
            throw new DownloadException("Error unrecognized file extension :" + downloadedPath);
        }
        return newComic;
    }

    private void slackNotify(ComicSearchDetailsDTO comicSearchDetailsDto, ComicEntity comicEntity, boolean Success) {
        if (configurationService.getConfiguration().getSlackConfiguration().isEnableNotifications()) {
            String Template = (Success) ? "Success" : "Error";
            try {
                slackNotifyService.sendNotification(Template, configurationService.getNotificationFile(), configurationService.getConfiguration().getSlackConfiguration().getSlackWebHook(), configurationService.getConfiguration().getSlackConfiguration().getComicVaultBaseUrl(), comicSearchDetailsDto, comicEntity);
            } catch (SlackNotifyException e) {
                logger.error("Error sending slack notification :{}", e.getMessage());
            }
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping(path = "/comics/{comicID}/download")
    public ResponseEntity<Resource> downloadComic(@PathVariable String comicID) throws EntityNotFoundException, DownloadToDeviceException {

        Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);

        if (comicEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(comicID, EntityNotFoundException.Entity.COMIC);
        }

        Path comicPath = Paths.get(comicEntityOptional.get().getPath());
        try {
            Resource resource = new UrlResource(comicPath.toUri());

            String filename = comicPath.getFileName().toString();

            // Set content type based on file extension
            String contentType = "application/octet-stream";  // Default type

            if (filename.endsWith(".cbr")) {
                contentType = "application/x-rar-compressed";  // CBR file type
            } else if (filename.endsWith(".cbz")) {
                contentType = "application/zip";  // CBZ file type
            }

            // Set the Content-Disposition header for file download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);  // Set the correct content type

            return ResponseEntity.status(HttpStatus.OK)
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            throw new DownloadToDeviceException("Could not download on device: " + comicPath.toString());
        }

    }

    // Post method as the list of urls could overflow the url max length
    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @PostMapping(path = "/comicList/download")
    public ResponseEntity<Resource> downloadListComics(@RequestBody List<String> comicIDs) throws EntityNotFoundException, FileManagerException, DownloadToDeviceException {
        List<ComicEntity> listComics = new ArrayList<>();
        for (String comicID : comicIDs) {
            Optional<ComicEntity> comicEntityOptional = comicService.getComicByID(comicID);
            if (comicEntityOptional.isEmpty()) {
                throw new EntityNotFoundException(comicID, EntityNotFoundException.Entity.COMIC);
            } else {
                listComics.add(comicEntityOptional.get());
            }
        }

        List<String> pathFiles = listComics.stream()
                .map(ComicEntity::getPath)
                .collect(Collectors.toList());
        // Create a temporary ZIP file
        Path zipPath = fileManagerService.createZip(pathFiles);
        try {
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(zipPath));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comics.zip");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            throw new DownloadToDeviceException("Could not download on device: " + zipPath.toString());
        }
    }

    @Transactional
    private ComicEntity addComicToDataBaseComplete(Path file, JobEntity jobEntity, @Nullable ComicSearchDetailsDTO comicSearchDetailsDto) throws FileManagerException, ComicMetaDataException, ComicFileException {
        /*Quick check if there is a mismatch on file path and comic search info. If so, move the file*/
        Path seriesPath = file.getParent();
        Path publisherPath = seriesPath.getParent();
        if ((comicSearchDetailsDto != null && Files.exists(seriesPath) && !comicSearchDetailsDto.getSeries().equals(seriesPath.getFileName().toString())) ||
                (comicSearchDetailsDto != null && Files.exists(publisherPath) && !comicSearchDetailsDto.getCategory().equals(publisherPath.getFileName().toString()))) {
            Path targetPath = Paths.get(configurationService.getConfiguration().getDownloadRoot(), comicSearchDetailsDto.getCategory(), comicSearchDetailsDto.getSeries(), file.getFileName().toString());
            fileManagerService.moveFile(file, targetPath);
            file = targetPath;
        }

        ComicDTO comicDto = comicMetaDataService.getComicMetaData(file, comicSearchDetailsDto, configurationService.getConfiguration().getGetComicsBaseUrl());
        // Store tags previously
        if (comicSearchDetailsDto != null && comicSearchDetailsDto.getTags() != null) {
            for (TagDTO tagDTO : comicSearchDetailsDto.getTags()) {
                if (tagService.findName(tagDTO.getName()).isEmpty()) {
                    TagEntity tagEntity = tagMapper.mapFrom(tagDTO);
                    tagService.save(tagEntity);
                }
            }
        }


        ComicEntity comicEntity = comicMapper.mapFrom(comicDto);
        SeriesEntity seriesEntity;
        if (seriesService.getSeriesByID(comicDto.getSeriesID()).isEmpty()) {
            SeriesDTO seriesDto = comicMetaDataService.getSeriesMetaData(comicDto);
            seriesEntity = seriesMapper.mapFrom(seriesDto);
            seriesService.createSeries(seriesEntity);
            LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity("Info").severityMessage("New Series").username(jobEntity.getUsername()).message(seriesEntity.getTitle()).seriesId(seriesEntity.getSeriesID()).build();
            addLog(logDTO, jobEntity);
        } else {
            seriesEntity = seriesService.getSeriesByID(comicDto.getSeriesID()).get();
        }


        fileManagerService.getSmallCoverPath(configurationService.getConfiguration().getDownloadRoot(), true);
        fileManagerService.getMediumCoverPath(configurationService.getConfiguration().getDownloadRoot(), true);
        Path coverPathSmall = fileManagerService.getSmallCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicDto.getId(), false);
        Path coverPathMedium = fileManagerService.getMediumCoverFilePath(configurationService.getConfiguration().getDownloadRoot(), comicDto.getId(), false);


        comicFileService.savePageToFile(file, coverPathSmall, 0, 300);
        comicFileService.savePageToFile(file, coverPathMedium, 0, 600);
        // DO NOT add entity on service, the series will do the job
        if (!seriesEntity.getComics().contains(comicEntity)) {
            seriesEntity.addComic(comicEntity);
        }
        seriesEntity.setModifiedAt(new Date());
        LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity("Info").severityMessage("New Comic").username(jobEntity.getUsername()).message(comicEntity.getTitle()).comicId(comicEntity.getId()).build();
        addLog(logDTO, jobEntity);
        seriesService.save(seriesEntity);

        //Generate mini-thumbnails
        if (configurationService.getConfiguration().isGenerateNavigationThumbnails()) {
            Path miniPagesFolder = fileManagerService.getMiniPagesFolderPath(configurationService.getConfiguration().getDownloadRoot(), comicDto.getId(), false, true);
            record ComicJobArgs(Path comicFile, Path miniPagesFolder, int resolution) {
            }
            ;
            schedulerService.schedule(args -> {
                try {
                    comicFileService.savePagesToFolder(args.comicFile(), args.miniPagesFolder(), args.resolution());
                } catch (ComicFileException e) {
                    logger.error("Error generating mini pages '{}': {}", args.comicFile(), e.getMessage());
                }
            }, new ComicJobArgs(file, miniPagesFolder, 200));
        }


        return comicEntity;
    }


    private void addLog(LogDTO logDto, JobEntity job) {
        LogEntity log = logMapper.mapFrom(logDto);
        log.setJobId(job.getJobId());
        logService.save(log);
        job.getLogsIds().add(log.getLogId());
        jobService.save(job);
    }


    private void printStrackTrace(Exception e) {
        e.printStackTrace();
    }

}

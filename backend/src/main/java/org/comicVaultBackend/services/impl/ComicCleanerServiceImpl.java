package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.domain.dto.LogDTO;
import org.comicVaultBackend.domain.entities.*;
import org.comicVaultBackend.exceptions.FileManagerException;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Service
public class ComicCleanerServiceImpl implements ComicCleanerService {
    @Autowired
    private FileManagerService fileManagerService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private SeriesService seriesService;

    @Autowired
    private ComicService comicService;

    @Autowired
    private LogService logService;

    @Autowired
    private JobService jobService;

    @Autowired
    private Mapper<LogEntity, LogDTO> logMapper;

    private static final Logger logger = LoggerFactory.getLogger(ComicCleanerServiceImpl.class);

    @Override
    public String deleteComic(ComicEntity comicEntity, boolean deleteFiles, boolean deleteSeries, JobEntity jobEntity) throws FileManagerException {

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


        logService.removeComicIdFromLog(comicId);
        LogDTO logDTO = LogDTO.builder().timeStamp(new Date()).severity(LogEntity.LogSeverity.Warning).severityMessage("Delete Comic").username(jobEntity.getUsername()).message(comicEntity.getTitle()).build();
        addLog(logDTO, jobEntity);

        if (seriesEntity.getComics().isEmpty() && deleteSeries) {
            seriesService.deleteSeriesById(seriesEntity.getSeriesID());
            this.logService.removeSeriesIdFromLog(seriesId);
            LogDTO logDTOSeries = LogDTO.builder().timeStamp(new Date()).severity(LogEntity.LogSeverity.Warning).severityMessage("Delete Series").username(jobEntity.getUsername()).message(seriesEntity.getTitle()).build();
            addLog(logDTOSeries, jobEntity);
            if (deleteFiles) {
                try {
                    fileManagerService.deleteFolder(parentPath);
                } catch (FileManagerException e) {
                    logger.error("Could not delete folder: {}", parentPath.toString());
                }
            }
        }
        return nextComicID;
    }

    @Override
    public void deleteSeries(SeriesEntity seriesEntity, boolean deleteFiles, JobEntity jobEntity) throws FileManagerException {
        Path pathSeries = null;
        for (ComicEntity comic : seriesEntity.getComics()) {
            if (pathSeries == null) {
                String comicPath = comic.getPath();
                Path path = Paths.get(comicPath);
                pathSeries = path.getParent();
            }
            deleteComic(comic, deleteFiles, false, jobEntity);
        }
        seriesService.deleteSeriesById(seriesEntity.getSeriesID());
        this.logService.removeSeriesIdFromLog(seriesEntity.getSeriesID());
        LogDTO logDTOSeries = LogDTO.builder().timeStamp(new Date()).severity(LogEntity.LogSeverity.Warning).severityMessage("Delete Series").username(jobEntity.getUsername()).message(seriesEntity.getTitle()).build();
        addLog(logDTOSeries, jobEntity);
        if (deleteFiles && pathSeries != null) {
            try {
                fileManagerService.deleteFolder(pathSeries);
            } catch (FileManagerException e) {
                logger.error("Could not delete folder: {}", pathSeries.toString());
            }
        }
    }

    private void addLog(LogDTO logDto, JobEntity job) {
        LogEntity log = logMapper.mapFrom(logDto);
        log.setJobId(job.getJobId());
        logService.save(log);
        job.getLogsIds().add(log.getLogId());
        jobService.save(job);
    }
}

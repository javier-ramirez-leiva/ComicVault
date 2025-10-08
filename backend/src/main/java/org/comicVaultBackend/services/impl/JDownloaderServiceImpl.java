package org.comicVaultBackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.comicVaultBackend.domain.dto.*;
import org.comicVaultBackend.exceptions.DownloadException;
import org.comicVaultBackend.services.JDownloaderService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class JDownloaderServiceImpl implements JDownloaderService {

    public Path downloadFile(Path downloadRoot, Path parentFolder, DownloadLinkDTO downloadLink, String title, ComicSearchDTO comicSearchDto, String description, JDownloaderConfigurationDTO jDownloaderConfiguration) throws DownloadException {
        /*Create a comic.gcd json formatted file with all information of the comic, so that if later it's scanned the information will be retrieved*/
        ComicSearchDetailsDTO comicSearchDetailsDto = new ComicSearchDetailsDTO(comicSearchDto);
        comicSearchDetailsDto.setDescription(description);
        String JSONString = comicSearchDetailsDto.ToJSONString();
        Path jdownloadFolderPath = Paths.get(downloadRoot.toString(), comicSearchDto.getIdGc());
        try {
            Files.createDirectories(jdownloadFolderPath);
        } catch (IOException e) {
            throw new DownloadException("Folder matching JDownloader folder could not be created: " + jdownloadFolderPath.toString());
        }
        Path gcdFilePath = Paths.get(jdownloadFolderPath.toString(), "comic.gdc");
        try (FileWriter writer = new FileWriter(gcdFilePath.toString())) {
            if (!Files.exists(jdownloadFolderPath)) {
                Files.createDirectories(jdownloadFolderPath);
            }
            writer.write(JSONString);
        } catch (IOException e) {
            throw new DownloadException("comic.gcd could not be created: " + gcdFilePath.toString());
        }

        CrawljobDTO job = CrawljobDTO.builder().text(downloadLink.getLink()).packageName(comicSearchDto.getIdGc()).downloadFolder(jDownloaderConfiguration.getJDownloaderOutputPath()).build();

        Path jsonFilePath = Paths.get(jDownloaderConfiguration.getJDownloaderCrawljobPath(), comicSearchDto.getIdGc() + ".crawljob");

        ObjectMapper objectMapper = new ObjectMapper();

        List<CrawljobDTO> jobs = List.of(job);

        try {
            // Write the DTO object as JSON to the file
            objectMapper.writeValue(jsonFilePath.toFile(), jobs);
        } catch (IOException e) {
            throw new DownloadException("Job could not be created: " + jsonFilePath.toString());
        }

        /* If the folder downloadRoot/id_gc exists the crawljob was added*/
        File folderJob = jdownloadFolderPath.toFile();

        comicSearchDto.setTotalBytes(_convertToBytes(comicSearchDto.getSize()));
        comicSearchDto.setDownloadingStatus("downloading");
        try {
            String comicFilePath = _updateComicFilesProgress(folderJob, comicSearchDto);
            File comicFile = new File(comicFilePath);
            if (!comicFile.exists()) {
                throw new DownloadException("Grab error. Comic file is empty");
            }
            Path sourcePath = comicFile.toPath();
            Path targetPath = Paths.get(parentFolder.toString(), comicFile.getName());
            try {
                Files.move(sourcePath, targetPath);
                if (jDownloaderConfiguration.isDeleteFolderOutputFolder()) {
                    File oldParentFolder = comicFile.getParentFile();
                    if (_deleteFolder(oldParentFolder)) {
                        System.out.println("Deleted: " + oldParentFolder.toString());
                    } else {
                        System.out.println("Failed to delete: " + oldParentFolder.toString());
                    }
                }
                return targetPath;
            } catch (Exception e) {
                throw new DownloadException("Move error: " + sourcePath.toString() + " -> " + targetPath.toString());
            }

        } catch (Exception e) {
            throw e;
        }
    }

    static String _updateComicFilesProgress(File folder, ComicSearchDTO comicSearch) throws DownloadException {

        File partFile = _wait1MinForPartFileToAppear(folder);

        //Be careful. The thread will wait an hour for the comic to appear. Is it the best choice? At least it works
        String comicFilePath = null;
        FilenameFilter comicFilter = (dir, name) -> (!name.toLowerCase().startsWith("_") && (name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".cbz")));
        int maxTimes = 720; //3600 seconds / 5 seconds iteration
        int times = 0;
        while (comicFilePath == null && maxTimes > times) {
            File[] comicFiles = folder.listFiles(comicFilter);
            if (comicFiles != null && comicFiles.length == 1) {
                comicFilePath = comicFiles[0].getAbsolutePath();
                System.out.println("Grabbed comics:" + comicFilePath);
                // Give JDownloader some 15 sec to do its thing and avoid potential errors
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    System.out.println("Thread was interrupted!");
                }
            } else {
                comicSearch.setCurrentBytes(partFile.length());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Thread was interrupted!");
                }
            }
            times++;
        }


        return comicFilePath;

        /*In order to calculate the progress, calculate each 5s the difference between the nominal size and the size of the part file found on the folder (there is only one)*/
        /*AtomicReference<String> comicFilePath = new AtomicReference<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);  // To block the caller until task finishes
        Runnable task = () -> {
            FilenameFilter comicFilter = (dir, name) -> (!name.toLowerCase().startsWith("_") && (name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".cbz")));
            File[] comicFiles = folder.listFiles(comicFilter);
            if(comicFiles!=null && comicFiles.length==1){
                comicFilePath.set(comicFiles[0].getAbsolutePath());
                System.out.println("Grabbed comics:" + comicFilePath);
                latch.countDown();  // Release the latch
                scheduler.shutdown();  // Stop the scheduler
            }else{
                comicSearch.setCurrentBytes(partFile.length());
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
        try {
            latch.await();  // Wait until the folder check finishes (either success or error)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return comicFilePath.get();*/
    }

    static File _wait1MinForPartFileToAppear(File folder) throws DownloadException {
        FilenameFilter partFilter = (dir, name) -> name.toLowerCase().endsWith(".part");
        AtomicReference<File> file = new AtomicReference<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);  // To block the caller until task finishes
        Runnable task = () -> {
            File[] files = folder.listFiles(partFilter);
            if (files != null && files.length == 1) {
                latch.countDown();  // Release the latch
                scheduler.shutdown();  // Stop the scheduler
                file.set(files[0]);
                System.out.println("Crawljob added:" + files[0].toString());
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
        try {
            // Wait for 1 minute
            boolean completed = latch.await(1, TimeUnit.MINUTES);
            if (!completed) {
                // Timeout occurred
                scheduler.shutdownNow();  // Cancel scheduled tasks
                throw new DownloadException("Check you JDownloader configuration.No part file found folder : " + folder.toString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DownloadException("Thread was interrupted while waiting for the .part file.", e);
        } finally {
            scheduler.shutdownNow();  // Ensure the scheduler is properly terminated
        }

        return file.get();
    }


    public static Long _convertToBytes(String sizeStr) {
        sizeStr = sizeStr.trim().toUpperCase();  // Normalize the string

        // Regular expression to split number and unit
        String[] parts = sizeStr.split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid file size format");
        }

        double sizeValue = Double.parseDouble(parts[0]);
        String unit = parts[1];

        switch (unit) {
            case "GB":
                return (long) (sizeValue * 1024 * 1024 * 1024);  // Convert GB to bytes
            case "MB":
                return (long) (sizeValue * 1024 * 1024);  // Convert MB to bytes
            case "KB":
                return (long) (sizeValue * 1024);  // Convert KB to bytes
            case "B":
                return (long) sizeValue;  // Bytes
            default:
                throw new IllegalArgumentException("Unknown size unit: " + unit);
        }
    }


    // Method to recursively delete a folder and its contents
    public static boolean _deleteFolder(File folder) {
        if (folder.isDirectory()) {
            // Get all files and subdirectories
            File[] files = folder.listFiles();
            if (files != null) {  // If folder is not empty
                for (File file : files) {
                    // Recursively delete subdirectories and files
                    _deleteFolder(file);
                }
            }
        }
        // Delete the folder or file
        return folder.delete();
    }
}

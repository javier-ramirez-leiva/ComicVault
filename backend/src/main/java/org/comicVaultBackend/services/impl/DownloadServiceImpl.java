package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.domain.dto.ComicSearchDTO;
import org.comicVaultBackend.domain.dto.DownloadLinkDTO;
import org.comicVaultBackend.domain.dto.JDownloaderConfigurationDTO;
import org.comicVaultBackend.exceptions.DownloadException;
import org.comicVaultBackend.services.DownloadService;
import org.comicVaultBackend.services.JDownloaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DownloadServiceImpl implements DownloadService {


    private List<ComicSearchDTO> listDownloadingComics = new ArrayList<>();

    @Autowired
    private JDownloaderService jDownloaderService;

    @Override
    public Path downloadFile(Path downloadRoot, Path parentFolder, DownloadLinkDTO downloadLink, String title, ComicSearchDTO comicSearchDto, String description, JDownloaderConfigurationDTO jDownloaderConfiguration) throws DownloadException {

        if (!listDownloadingComics.contains(comicSearchDto)) {
            listDownloadingComics.add(comicSearchDto);
        } else {
            comicSearchDto.setCurrentComic(comicSearchDto.getCurrentComic() + 1);
        }

        // Ensure the parent folder exists or create it if it doesn't
        try {
            if (!Files.exists(parentFolder)) {
                Files.createDirectories(parentFolder);
            }
        } catch (IOException e) {
            throw new DownloadException("Failed to create parent folder", e);
        }

        if (downloadLink.getLink() == null || downloadLink.getLink().isEmpty()) {
            throw new DownloadException("No download links provided");
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (downloadLink.getPlatform().equalsIgnoreCase("Download Now") || downloadLink.getPlatform().equalsIgnoreCase("Main Server")) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(downloadLink.getLink()).openConnection();
                connection.setInstanceFollowRedirects(true);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode >= 300 && responseCode < 400) {
                    // Handle redirect
                    String redirectedUrl = connection.getHeaderField("Location");
                    connection = (HttpURLConnection) new URL(redirectedUrl).openConnection();
                    connection.connect();
                }

                String contentType = connection.getContentType();

                /*Try direct url downloading*/
                if (contentType != null && (contentType.contains("application") || contentType.contains("octet-stream"))) {
                    // Get the final URL after all redirects
                    String finalURL = connection.getURL().toString();
                    String filenameURL = finalURL.substring(finalURL.lastIndexOf('/') + 1);
                    String extension = filenameURL.contains(".") ? filenameURL.substring(filenameURL.lastIndexOf('.')) : ".zip";

                    long fileSize = connection.getContentLengthLong();
                    long totalBytesRead = 0;

                    comicSearchDto.setTotalBytes(fileSize);
                    Path filePath = parentFolder.resolve(title + extension);

                    try (InputStream in = connection.getInputStream();
                         FileOutputStream fos = new FileOutputStream(filePath.toFile())) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            comicSearchDto.setCurrentBytes(totalBytesRead);
                        }
                    }

                    return filePath;
                }

            } catch (IOException e) {
                System.out.println("No downloading file from URL: " + downloadLink.getLink());
            }
        } else {
            try {
                return jDownloaderService.downloadFile(downloadRoot, parentFolder, downloadLink, title, comicSearchDto, description, jDownloaderConfiguration);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    @Override
    public List<Path> extractComics(Path zipFolder) throws DownloadException {
        return List.of();
    }

    @Override
    public List<ComicSearchDTO> getListDownloadingComics() {
        return listDownloadingComics;
    }

    @Override
    public void removeComicFromDownloadList(ComicSearchDTO comicSearchDTO) {
        listDownloadingComics.remove(comicSearchDTO);
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

}

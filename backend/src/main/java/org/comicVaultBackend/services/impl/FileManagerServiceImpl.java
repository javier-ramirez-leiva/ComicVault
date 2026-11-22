package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.exceptions.FileManagerException;
import org.comicVaultBackend.services.FileManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

@Service
public class FileManagerServiceImpl implements FileManagerService {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerServiceImpl.class);

    @Override
    public void throwIfNotExist(Path path) throws FileManagerException {
        if (Files.notExists(path)) {
            String message = "File or folder does not exists: " + path.toString();
            logger.error(message);
            throw new FileManagerException(message);
        }

    }

    @Override
    public Path createZip(List<String> filePaths) throws FileManagerException {
        try {
            Path zipPath = Files.createTempFile("comics-", ".zip");
            try (OutputStream outputStream = Files.newOutputStream(zipPath)) {
                writeZip(filePaths, outputStream);
            }
            return zipPath;
        } catch (Exception e) {
            logger.error("Temporary zip could not be created", e);
            throw new FileManagerException("Temporary zip could not be created", e);
        }
    }

    @Override
    public void writeZip(List<String> filePaths, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[64 * 1024];
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream, buffer.length))) {
            // CBR and CBZ are already compressed archives. Recompressing them costs CPU with
            // virtually no size benefit, so use the fastest valid ZIP mode.
            zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
            for (String filePath : filePaths) {
                Path path = Paths.get(filePath);
                if (Files.exists(path) && Files.isReadable(path)) {
                    zipOutputStream.putNextEntry(new ZipEntry(path.getFileName().toString()));
                    try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(path), buffer.length)) {
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            zipOutputStream.write(buffer, 0, bytesRead);
                        }
                    } finally {
                        zipOutputStream.closeEntry();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("ZIP stream could not be created", e);
            throw e;
        }
    }

    @Override
    public List<Path> findFilesWithExtensions(Path rootDirectory, String... extensions) throws FileManagerException {
        List<Path> result = new ArrayList<>();

        // Use Files.walk to traverse directories recursively
        try (Stream<Path> paths = Files.walk(rootDirectory)) {
            paths
                    .filter(Files::isRegularFile) // Filter to include only files
                    .filter(path -> {
                        for (String extension : extensions) {
                            if (path.toString().endsWith(extension)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .forEach(result::add);
        } catch (IOException e) {
            String message = "Error listing files '" + Arrays.toString(extensions) + "' : " + rootDirectory.toString();
            logger.error(message);
            throw new FileManagerException(message, e);
        }
        return result;
    }

    @Override
    public List<Path> listDirectFolders(Path rootDirectory) throws FileManagerException {
        List<Path> result = new ArrayList<>();

        // Use Files.walk to traverse directories recursively
        try (Stream<Path> paths = Files.walk(rootDirectory)) {
            paths
                    .filter(Files::isDirectory)
                    .filter(path -> path.compareTo(rootDirectory) != 0) // Root directory is listed
                    .forEach(result::add);
        } catch (IOException e) {
            String message = "Error listing direct folders: " + rootDirectory.toString();
            logger.error(message);
            throw new FileManagerException(message, e);
        }
        return result;
    }


    @Override
    public void deleteFile(Path path) throws FileManagerException {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            String message = "Error deleting file:" + path.toString();
            logger.error(message);
            throw new FileManagerException(message, e);
        }
    }

    @Override
    public void deleteFolder(Path path) throws FileManagerException {
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            }
        } catch (UncheckedIOException | IOException e) {
            String message = "Error deleting folder: " + path;
            logger.error(message, e);
            throw new FileManagerException(message, e);
        }
    }

    @Override
    public void moveFile(Path sourcePath, Path targetPath) throws FileManagerException {
        try {
            Files.createDirectories(targetPath.getParent());
            Files.move(sourcePath, targetPath);
        } catch (IOException e) {
            String message = "Error moving file:" + sourcePath.toString() + " -> " + targetPath.toString();
            logger.error(message);
            throw new FileManagerException(message, e);
        }
    }

    @Override
    public Path getSmallCoverPath(String downloadRoot, boolean createFolders) throws FileManagerException {
        Path parentFolder = Paths.get(downloadRoot, "covers", "small");
        if (createFolders) {
            createFolders(parentFolder);
        }
        return parentFolder;
    }

    @Override
    public Path getMediumCoverPath(String downloadRoot, boolean createFolders) throws FileManagerException {
        Path parentFolder = Paths.get(downloadRoot, "covers", "medium");
        if (createFolders) {
            createFolders(parentFolder);
        }
        return parentFolder;
    }

    @Override
    public Path getMiniPagesPath(String downloadRoot, boolean createFolders) throws FileManagerException {
        Path parentFolder = Paths.get(downloadRoot, "covers", "mini");
        if (createFolders) {
            createFolders(parentFolder);
        }
        return parentFolder;
    }

    @Override
    public Path getComicPathParentFolder(String downloadRoot, String category, String series, boolean createFolders) throws FileManagerException {
        Path parentFolder = Paths.get(downloadRoot, category, series);
        if (createFolders) {
            createFolders(parentFolder);
        }
        return parentFolder;
    }

    @Override
    public Path getSmallCoverFilePath(String downloadRoot, String id, boolean throwIfNotExist) throws FileManagerException {
        Path coversPath = getSmallCoverPath(downloadRoot, false);
        Path fileCover = Paths.get(coversPath.toString(), id + ".jpg");

        if (throwIfNotExist) {
            fileDoesNotExistsThrows(fileCover);
        }
        return fileCover;
    }

    @Override
    public Path getMediumCoverFilePath(String downloadRoot, String id, boolean throwIfNotExist) throws FileManagerException {
        Path coversPath = getMediumCoverPath(downloadRoot, false);
        Path fileCover = Paths.get(coversPath.toString(), id + ".jpg");

        if (throwIfNotExist) {
            fileDoesNotExistsThrows(fileCover);
        }
        return fileCover;
    }

    @Override
    public Path getMiniPagesFolderPath(String downloadRoot, String id, boolean throwIfNotExist, boolean createFolders) throws FileManagerException {
        Path miniPagesPath = getMiniPagesPath(downloadRoot, false);
        Path pagesFolder = Paths.get(miniPagesPath.toString(), id);

        if (throwIfNotExist) {
            folderDoesNotExistsThrows(pagesFolder);
        } else if (createFolders) {
            createFolders(pagesFolder);
        }
        return pagesFolder;
    }

    void createFolders(Path folders) throws FileManagerException {
        try {
            if (!Files.exists(folders)) {
                Files.createDirectories(folders);
            }
        } catch (IOException e) {
            String message = "Failed to create folder:" + folders.toString();
            logger.error(message);
            throw new FileManagerException(message, e);
        }
    }

    void fileDoesNotExistsThrows(Path file) throws FileManagerException {
        if (!Files.exists(file)) {
            String message = "File does not exist: " + file.toString();
            logger.error(message);
            throw new FileManagerException(message + file.toString());
        }
    }

    void folderDoesNotExistsThrows(Path folder) throws FileManagerException {
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            String message = "Folder does not exist: " + folder.toString();
            logger.error(message);
            throw new FileManagerException(message + folder.toString());
        }
    }

}

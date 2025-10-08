package org.comicVaultBackend.services;

import org.comicVaultBackend.exceptions.ComicFileException;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;

public interface ComicFileService {

    Integer getPages(Path comicFilePath) throws ComicFileException;

    List<Integer> listDoublePages(Path comicFilePath) throws ComicFileException;

    void savePageToFile(Path comicFilePath, Path storeFilePath, Integer page, int targetWidth) throws ComicFileException;

    void savePagesToFolder(Path comicFilePath, Path storeFolderPath, int targetWidth) throws ComicFileException;

    ByteArrayOutputStream getPage(Path comicFilePath, Integer page) throws ComicFileException;

    ByteArrayOutputStream getDoublePage(Path comicFilePath, Integer page) throws ComicFileException;

    List<Path> extract(Path archivePath, boolean deleteArchive) throws ComicFileException;
}

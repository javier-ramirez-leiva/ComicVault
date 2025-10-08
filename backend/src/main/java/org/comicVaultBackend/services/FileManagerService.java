package org.comicVaultBackend.services;

import org.comicVaultBackend.exceptions.FileManagerException;

import java.nio.file.Path;
import java.util.List;

public interface FileManagerService {
    Path createZip(List<String> filePaths) throws FileManagerException;

    List<Path> findFilesWithExtensions(Path rootDirectory, String... extensions) throws FileManagerException;

    List<Path> listDirectFolders(Path rootDirectory) throws FileManagerException;

    void throwIfNotExist(Path path) throws FileManagerException;

    void deleteFile(Path path) throws FileManagerException;

    void deleteFolder(Path path) throws FileManagerException;

    void moveFile(Path sourcePath, Path targetPath) throws FileManagerException;

    Path getSmallCoverPath(String downloadRoot, boolean createFolders) throws FileManagerException;

    Path getMediumCoverPath(String downloadRoot, boolean createFolders) throws FileManagerException;

    Path getMiniPagesPath(String downloadRoot, boolean createFolders) throws FileManagerException;

    Path getSmallCoverFilePath(String downloadRoot, String id, boolean throwIfNotExist) throws FileManagerException;

    Path getMediumCoverFilePath(String downloadRoot, String id, boolean throwIfNotExist) throws FileManagerException;

    Path getMiniPagesFolderPath(String downloadRoot, String id, boolean throwIfNotExist, boolean createFolders) throws FileManagerException;

    Path getComicPathParentFolder(String downloadRoot, String category, String series, boolean createFolders) throws FileManagerException;
}

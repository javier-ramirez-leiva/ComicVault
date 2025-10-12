package org.comicVaultBackend.services.impl;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import net.coobird.thumbnailator.Thumbnails;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.comicVaultBackend.exceptions.ComicFileException;
import org.comicVaultBackend.services.ComicFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Service
public class ComicFileServiceImpl implements ComicFileService {

    private static final Logger logger = LoggerFactory.getLogger(ComicFileServiceImpl.class);

    @Override
    public Integer getPages(Path comicFilePath) throws ComicFileException {

        String filePath = comicFilePath.toString();

        try {
            if (filePath.contains(".cbr")) {
                try {
                    return _countPagesInCBR(filePath);
                } catch (Exception e) {
                    return _countPagesLib(filePath);
                }
            } else if (filePath.contains(".cbz")) {
                try {
                    return _countPagesInCBZ(filePath);
                } catch (Exception e) {
                    return _countPagesLib(filePath);
                }
            } else {
                String message = "File is not a comic: " + filePath;
                logger.error(message);
                throw new ComicFileException(message);
            }
        } catch (Exception e) {
            String message = "Could not count pages on comic: " + filePath;
            logger.error(message);
            throw new ComicFileException(message, e);
        }
    }

    @Override
    public List<Integer> listDoublePages(Path comicFilePath) throws ComicFileException {

        List<Integer> doublePages = new ArrayList<>();
        Integer pages = getPages(comicFilePath);
        for (int i = 0; i < pages; ++i) {
            ByteArrayOutputStream bytes = getPage(comicFilePath, i);

            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes.toByteArray()));
                // --- Detect if img1 is already a double page ---
                double ratio = (double) img.getWidth() / img.getHeight();
                if (ratio > 1) {
                    doublePages.add(i);
                }
            } catch (Exception e) {
                throw new ComicFileException("Could identify page " + i);
            }
        }

        return doublePages;
    }

    @Override
    public void savePageToFile(Path comicFilePath, Path storeFilePath, Integer page, int targetWidth) throws ComicFileException {

        String filePath = comicFilePath.toString();

        try {
            if (filePath.contains(".cbr")) {
                try {
                    _savePageToFileCBR(filePath, storeFilePath, page, targetWidth);
                } catch (Exception e) {
                    _savePageToFileLib(filePath, storeFilePath, page, targetWidth);
                }
            } else if (filePath.contains(".cbz")) {
                try {
                    _savePageToFileCBZ(filePath, storeFilePath, page, targetWidth);
                } catch (Exception e) {
                    _savePageToFileLib(filePath, storeFilePath, page, targetWidth);
                }
            } else {
                String message = "File is not a comic: " + filePath;
                logger.error(message);
                throw new ComicFileException(message);
            }
        } catch (Exception e) {
            String message = "Could not save page : " + filePath + " page:" + page;
            logger.error(message);
            throw new ComicFileException(message, e);
        }
    }

    @Override
    public void savePagesToFolder(Path comicFilePath, Path storeFolderPath, int targetWidth) throws ComicFileException {
        String filePath = comicFilePath.toString();

        try {
            if (filePath.contains(".cbr")) {
                try {
                    _savePagesToFolderCBR(filePath, storeFolderPath, targetWidth);
                } catch (Exception e) {
                    _savePagesToFolderLib(filePath, storeFolderPath, targetWidth);
                }
            } else if (filePath.contains(".cbz")) {
                try {
                    _savePagesToFolderCBZ(filePath, storeFolderPath, targetWidth);
                } catch (Exception e) {
                    _savePagesToFolderLib(filePath, storeFolderPath, targetWidth);
                }
            } else {
                String message = "File is not a comic: " + filePath;
                logger.error(message);
                throw new ComicFileException(message);
            }
        } catch (Exception e) {
            String message = "Could not save pages : " + filePath;
            logger.error(message);
            throw new ComicFileException(message, e);
        }
    }

    @Override
    public ByteArrayOutputStream getPage(Path comicFilePath, Integer page) throws ComicFileException {
        String filePath = comicFilePath.toString();

        try {
            if (filePath.contains(".cbr")) {
                try {
                    return _getPageFromCBR(filePath, page);
                } catch (Exception e) {
                    return _getPageFromLib(filePath, page);
                }
            } else if (filePath.contains(".cbz")) {
                try {
                    return _getPageFromCBZ(filePath, page);
                } catch (Exception e) {
                    return _getPageFromLib(filePath, page);
                }
            } else {
                String message = "File is not a comic: " + filePath;
                logger.error(message);
                throw new ComicFileException(message);
            }
        } catch (Exception e) {
            String message = "Could not get page : " + filePath + " page:" + page;
            logger.error(message);
            throw new ComicFileException(message, e);
        }
    }

    @Override
    public ByteArrayOutputStream getDoublePage(Path comicFilePath, Integer page) throws ComicFileException {
        try {
            // Get the first page as image
            ByteArrayOutputStream baos1 = getPage(comicFilePath, page);
            BufferedImage img1 = ImageIO.read(new ByteArrayInputStream(baos1.toByteArray()));

            if (img1 == null) {
                throw new ComicFileException("Could not read image for page " + page);
            }

            // --- Detect if img1 is already a double page ---
            double ratio = (double) img1.getWidth() / img1.getHeight();
            if (ratio > 1) {
                return baos1;
            }

            // Get the second page (next one) as image
            ByteArrayOutputStream baos2 = getPage(comicFilePath, page + 1);
            BufferedImage img2 = ImageIO.read(new ByteArrayInputStream(baos2.toByteArray()));

            if (img1 == null || img2 == null) {
                throw new ComicFileException("Could not read images for pages " + page + " and " + (page + 1));
            }

            // Create combined image (width = sum, height = max)
            int combinedWidth = img1.getWidth() + img2.getWidth();
            int combinedHeight = Math.max(img1.getHeight(), img2.getHeight());
            BufferedImage combined = new BufferedImage(combinedWidth, combinedHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = combined.createGraphics();
            g.drawImage(img1, 0, 0, null);
            g.drawImage(img2, img1.getWidth(), 0, null);
            g.dispose();

            // Write combined image to ByteArrayOutputStream
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(combined, "jpg", output);

            return output;

        } catch (Exception e) {
            String message = "Could not get double page from " + comicFilePath + " starting at page: " + page;
            logger.error(message, e);
            throw new ComicFileException(message, e);
        }
    }

    @Override
    public List<Path> extract(Path archivePath, boolean deleteArchive) throws ComicFileException {
        String filePath = archivePath.toString();
        Path outputDir = archivePath.getParent();
        try {
            List<Path> listExtracted;
            if (filePath.contains(".zip")) {
                try {
                    listExtracted = extractZip(filePath, outputDir);
                } catch (Exception e) {
                    listExtracted = extractRAR(filePath, outputDir);
                }
            } else {
                try {
                    listExtracted = extractRAR(filePath, outputDir);
                } catch (Exception e) {
                    listExtracted = extractZip(filePath, outputDir);
                }
            }
            if (deleteArchive) {
                Files.deleteIfExists(archivePath);
            }
            return listExtracted;
        } catch (Exception e) {
            String message = "Could not extract archive : " + filePath;
            logger.error(message);
            throw new ComicFileException(message, e);
        }
    }

    private int _countPagesInCBZ(String filePath) throws IOException {
        int pageCount = 0;
        ZipFile zipFile = new ZipFile(new File(filePath));
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (_isImageFile(entry.getName())) {
                pageCount++;
            }
        }

        zipFile.close();
        return pageCount;
    }

    private void _savePageToFileCBZ(String filePath, Path storeFilePath, Integer page, int targetWidth) throws IOException {
        int pageCount = 0;
        ZipFile zipFile = new ZipFile(new File(filePath));
        Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
        List<ZipEntry> entries = new ArrayList<>();

        while (entriesEnum.hasMoreElements()) {
            entries.add(entriesEnum.nextElement());
        }

        // Sort entries by name
        entries.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));


        for (ZipEntry entry : entries) {
            if (_isImageFile(entry.getName())) {
                if (pageCount == page) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        BufferedImage image = ImageIO.read(is);
                        if (targetWidth > 0) {
                            BufferedImage resizedImage = Thumbnails.of(image).width(targetWidth).keepAspectRatio(true).asBufferedImage();
                            ImageIO.write(resizedImage, "jpg", new File(storeFilePath.toString()));
                        } else {
                            ImageIO.write(image, "jpg", new File(storeFilePath.toString()));
                        }
                        break;
                    }
                }
                pageCount++;
            }
        }

        zipFile.close();
    }

    private void _savePagesToFolderCBZ(String filePath, Path storeFolderPath, int targetWidth) throws IOException {
        int pageCount = 0;
        ZipFile zipFile = new ZipFile(new File(filePath));
        Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
        List<ZipEntry> entries = new ArrayList<>();

        while (entriesEnum.hasMoreElements()) {
            entries.add(entriesEnum.nextElement());
        }

        // Sort entries by name
        entries.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));


        for (ZipEntry entry : entries) {
            if (_isImageFile(entry.getName())) {
                Path pageFile = Paths.get(storeFolderPath.toString(), String.valueOf(pageCount) + ".jpg");
                try (InputStream is = zipFile.getInputStream(entry)) {
                    BufferedImage image = ImageIO.read(is);
                    if (targetWidth > 0) {
                        BufferedImage resizedImage = Thumbnails.of(image).width(targetWidth).keepAspectRatio(true).asBufferedImage();
                        ImageIO.write(resizedImage, "jpg", new File(pageFile.toString()));
                    } else {
                        ImageIO.write(image, "jpg", new File(pageFile.toString()));
                    }
                }
                pageCount++;
            }
        }

        zipFile.close();
    }

    private ByteArrayOutputStream _getPageFromCBZ(String filePath, Integer page) throws IOException, RarException {
        int pageCount = 0;
        ZipFile zipFile = new ZipFile(new File(filePath));
        Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
        List<ZipEntry> entries = new ArrayList<>();
        ByteArrayOutputStream outputStream = null;

        while (entriesEnum.hasMoreElements()) {
            entries.add(entriesEnum.nextElement());
        }

        // Sort entries by name
        entries.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));


        for (ZipEntry entry : entries) {
            if (_isImageFile(entry.getName())) {
                if (pageCount == page) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        outputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                    break;
                }
                pageCount++;
            }
        }

        zipFile.close();

        return outputStream;
    }

    private int _countPagesInCBR(String filePath) throws IOException, RarException {
        int pageCount = 0;
        Archive archive = new Archive(new File(filePath));
        List<FileHeader> fileHeaders = archive.getFileHeaders();

        for (FileHeader fileHeader : fileHeaders) {
            if (_isImageFile(fileHeader.getFileNameString())) {
                pageCount++;
            }
        }

        return pageCount;
    }

    private int _countPagesLib(String filePath) throws IOException {
        int pageCount = 0;

        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
        IInArchive inArchive = SevenZip.openInArchive(
                null, // Use RAR5 format for .cbr files
                new RandomAccessFileInStream(randomAccessFile)
        );

        ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

        for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
            String itemName = item.getPath();
            if (itemName != null && _isImageFile(itemName)) {
                pageCount++;
            }
        }

        inArchive.close();
        randomAccessFile.close();

        return pageCount;
    }

    private ByteArrayOutputStream _getPageFromCBR(String filePath, Integer page) throws IOException, RarException {
        Archive archive = new Archive(new File(filePath));
        List<FileHeader> fileHeaders = archive.getFileHeaders();

        // Sort file headers by file name
        ByteArrayOutputStream outputStream = null;
        int pageCount = 0;
        fileHeaders.sort(Comparator.comparing(FileHeader::getFileNameString, String.CASE_INSENSITIVE_ORDER));

        for (FileHeader fileHeader : fileHeaders) {
            if (_isImageFile(fileHeader.getFileNameString())) {
                if (pageCount == page) {
                    outputStream = new ByteArrayOutputStream();
                    archive.extractFile(fileHeader, outputStream);
                    break;
                }
                pageCount++;
            }
        }

        return outputStream;
    }

    private ByteArrayOutputStream _getPageFromLib(String filePath, Integer page) throws IOException, RarException {
        int pageCount = 0;
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        ByteArrayOutputStream outputStream = null;

        try {
            randomAccessFile = new RandomAccessFile(filePath, "r");
            inArchive = SevenZip.openInArchive(
                    null,
                    new RandomAccessFileInStream(randomAccessFile)
            );

            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                String itemName = item.getPath();
                if (itemName != null && _isImageFile(itemName)) {
                    if (pageCount == page) {
                        outputStream = new ByteArrayOutputStream();

                        ByteArrayOutputStream finalOutputStream = outputStream;
                        ExtractOperationResult result = item.extractSlow(data -> {
                            try {
                                finalOutputStream.write(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return data.length;
                        });

                        if (result != ExtractOperationResult.OK) {
                            throw new IOException("Failed to extract file: " + itemName + " (" + result + ")");
                        }
                        break;
                    }
                    pageCount++;
                }
            }

        } catch (SevenZipException e) {
            throw new IOException("SevenZip error reading archive", e);
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (SevenZipException ignored) {
                }
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
        return outputStream;
    }

    private void _savePageToFileCBR(String filePath, Path storeFilePath, Integer page, int targetWidth) throws IOException, RarException {
        int pageCount = 0;
        Archive archive = new Archive(new File(filePath));
        List<FileHeader> fileHeaders = archive.getFileHeaders();

        // Sort file headers by file name
        fileHeaders.sort(Comparator.comparing(FileHeader::getFileNameString, String.CASE_INSENSITIVE_ORDER));


        for (FileHeader fileHeader : fileHeaders) {
            if (_isImageFile(fileHeader.getFileNameString())) {
                if (pageCount == page) {
                    try (FileOutputStream fos = new FileOutputStream(storeFilePath.toFile())) {
                        archive.extractFile(fileHeader, fos);
                        if (targetWidth > 0) {
                            BufferedImage image = ImageIO.read(storeFilePath.toFile());
                            BufferedImage resizedImage = Thumbnails.of(image).width(targetWidth).keepAspectRatio(true).asBufferedImage();
                            // Delete previous image for the resized one
                            storeFilePath.toFile().delete();
                            ImageIO.write(resizedImage, "jpg", new File(storeFilePath.toString()));
                        }
                        break;
                    }
                }
                pageCount++;
            }
        }
    }

    private void _savePagesToFolderCBR(String filePath, Path storeFolderPath, int targetWidth) throws IOException, RarException {
        int pageCount = 0;
        Archive archive = new Archive(new File(filePath));
        List<FileHeader> fileHeaders = archive.getFileHeaders();

        // Sort file headers by file name
        fileHeaders.sort(Comparator.comparing(FileHeader::getFileNameString, String.CASE_INSENSITIVE_ORDER));


        for (FileHeader fileHeader : fileHeaders) {
            if (_isImageFile(fileHeader.getFileNameString())) {
                Path pageFile = Paths.get(storeFolderPath.toString(), String.valueOf(pageCount) + ".jpg");
                try (FileOutputStream fos = new FileOutputStream(pageFile.toFile())) {
                    archive.extractFile(fileHeader, fos);
                    if (targetWidth > 0) {
                        BufferedImage image = ImageIO.read(pageFile.toFile());
                        BufferedImage resizedImage = Thumbnails.of(image).width(targetWidth).keepAspectRatio(true).asBufferedImage();
                        // Delete previous image for the resized one
                        pageFile.toFile().delete();
                        ImageIO.write(resizedImage, "jpg", new File(pageFile.toString()));
                    }
                }
                pageCount++;
            }
        }
    }

    private void _savePageToFileLib(String filePath, Path storeFilePath, Integer page, int targetWidth) throws IOException {
        int pageCount = 0;
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;

        try {
            randomAccessFile = new RandomAccessFile(filePath, "r");
            inArchive = SevenZip.openInArchive(
                    null,
                    new RandomAccessFileInStream(randomAccessFile)
            );

            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                String itemName = item.getPath();
                if (itemName != null && _isImageFile(itemName)) {
                    if (pageCount == page) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        ExtractOperationResult result = item.extractSlow(data -> {
                            try {
                                baos.write(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return data.length;
                        });

                        if (result != ExtractOperationResult.OK) {
                            throw new IOException("Failed to extract file: " + itemName + " (" + result + ")");
                        }

                        byte[] imageBytes = baos.toByteArray();
                        Files.write(storeFilePath, imageBytes); // Write original image

                        // Resize if needed
                        if (targetWidth > 0) {
                            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                            BufferedImage resized = Thumbnails.of(image)
                                    .width(targetWidth)
                                    .keepAspectRatio(true)
                                    .asBufferedImage();
                            ImageIO.write(resized, "jpg", storeFilePath.toFile()); // overwrite
                        }

                        break;
                    }
                    pageCount++;
                }
            }

        } catch (SevenZipException e) {
            throw new IOException("SevenZip error reading archive", e);
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (SevenZipException ignored) {
                }
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }

    private void _savePagesToFolderLib(String filePath, Path storeFolderPath, int targetWidth) throws IOException {
        int pageCount = 0;
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;

        try {
            randomAccessFile = new RandomAccessFile(filePath, "r");
            inArchive = SevenZip.openInArchive(
                    null,
                    new RandomAccessFileInStream(randomAccessFile)
            );

            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                String itemName = item.getPath();
                if (itemName != null && _isImageFile(itemName)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    ExtractOperationResult result = item.extractSlow(data -> {
                        try {
                            baos.write(data);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return data.length;
                    });

                    if (result != ExtractOperationResult.OK) {
                        throw new IOException("Failed to extract file: " + itemName + " (" + result + ")");
                    }

                    Path pageFile = Paths.get(storeFolderPath.toString(), String.valueOf(pageCount) + ".jpg");
                    byte[] imageBytes = baos.toByteArray();
                    Files.write(pageFile, imageBytes); // Write original image

                    // Resize if needed
                    if (targetWidth > 0) {
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        BufferedImage resized = Thumbnails.of(image)
                                .width(targetWidth)
                                .keepAspectRatio(true)
                                .asBufferedImage();
                        ImageIO.write(resized, "jpg", pageFile.toFile()); // overwrite
                    }
                    pageCount++;
                }
            }

        } catch (SevenZipException e) {
            throw new IOException("SevenZip error reading archive", e);
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (SevenZipException ignored) {
                }
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }

    private List<Path> extractRAR(String filePath, Path outputDir) throws RarException, IOException {
        Archive archive = new Archive(new File(filePath));
        List<FileHeader> fileHeaders = archive.getFileHeaders();
        List<Path> extractedFiles = new ArrayList<>();

        // Sort file headers by file name
        fileHeaders.sort(Comparator.comparing(FileHeader::getFileNameString, String.CASE_INSENSITIVE_ORDER));
        for (FileHeader fileHeader : fileHeaders) {
            if (fileHeader.isDirectory()) {
                continue;
            }

            // Get the file name without any directory structure inside the archive
            String originalFileName = fileHeader.getFileNameString().replace("\\", "/");
            String baseFileName = Paths.get(originalFileName).getFileName().toString();

            Path outputFile = outputDir.resolve(baseFileName);

            // Ensure parent directories exist
            Files.createDirectories(outputFile.getParent());

            try (OutputStream os = new FileOutputStream(outputFile.toFile())) {
                archive.extractFile(fileHeader, os);
            }

            extractedFiles.add(outputFile);
        }
        return extractedFiles;
    }

    private List<Path> extractLib(String filePath, Path outputDir) throws IOException, SevenZipException {
        List<Path> extractedFiles = new ArrayList<>();
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;

        try {
            randomAccessFile = new RandomAccessFile(filePath, "r");
            inArchive = SevenZip.openInArchive(
                    null, // Auto-detect archive format
                    new RandomAccessFileInStream(randomAccessFile)
            );

            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
            ISimpleInArchiveItem[] items = simpleInArchive.getArchiveItems();

            // Sort items by file name (case-insensitive)
            Arrays.sort(items, Comparator.comparing(i -> {
                try {
                    return i.getPath();
                } catch (SevenZipException e) {
                    return "";
                }
            }, String.CASE_INSENSITIVE_ORDER));

            for (ISimpleInArchiveItem item : items) {
                if (item.isFolder()) {
                    continue;
                }

                String pathInArchive = item.getPath().replace("\\", "/");
                String baseFileName = Paths.get(pathInArchive).getFileName().toString();
                Path outputFile = outputDir.resolve(baseFileName);

                // Ensure directory exists
                Files.createDirectories(outputFile.getParent());

                try (OutputStream out = Files.newOutputStream(outputFile)) {
                    ExtractOperationResult result = item.extractSlow(data -> {
                        try {
                            out.write(data);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return data.length;
                    });

                    if (result != ExtractOperationResult.OK) {
                        logger.warn("Failed to extract: " + baseFileName);
                        continue;
                    }
                } catch (Exception e) {
                    throw new IOException("SevenZip could not extract: " + filePath, e);
                }

                extractedFiles.add(outputFile);
            }
        } finally {
            if (inArchive != null) {
                inArchive.close();
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }

        return extractedFiles;
    }

    private List<Path> extractZip(String filePath, Path outputDir) throws RarException, IOException {
        ZipFile zipFile = new ZipFile(new File(filePath));
        Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
        List<ZipEntry> entries = new ArrayList<>();
        List<Path> extractedFiles = new ArrayList<>();

        while (entriesEnum.hasMoreElements()) {
            entries.add(entriesEnum.nextElement());
        }

        // Sort entries by name (case-insensitive)
        entries.sort(Comparator.comparing(ZipEntry::getName, String.CASE_INSENSITIVE_ORDER));

        for (ZipEntry entry : entries) {
            if (entry.isDirectory()) {
                continue;
            }

            // Flatten the file name (discard any internal folder structure)
            String originalName = entry.getName().replace("\\", "/");
            String baseFileName = Paths.get(originalName).getFileName().toString();

            Path outputFile = outputDir.resolve(baseFileName);

            // Ensure parent directories exist
            Files.createDirectories(outputFile.getParent());

            try (InputStream is = zipFile.getInputStream(entry);
                 OutputStream os = Files.newOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            extractedFiles.add(outputFile);
        }

        zipFile.close();
        return extractedFiles;
    }


    private boolean _isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") ||
                lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".gif") ||
                lowerCaseName.endsWith(".bmp");
    }
}

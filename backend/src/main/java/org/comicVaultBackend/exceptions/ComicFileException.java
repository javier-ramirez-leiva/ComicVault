package org.comicVaultBackend.exceptions;

public class ComicFileException extends Exception {
    public ComicFileException(String message) {
        super(message);
    }

    public ComicFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.comicVaultBackend.exceptions;

public class ComicScrapperParsingException extends Exception {
    public ComicScrapperParsingException(String message) {
        super(message);
    }

    public ComicScrapperParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.comicVaultBackend.exceptions;

public class ComicTitleParserServiceException extends Exception {
    public ComicTitleParserServiceException(String message) {
        super(message);
    }

    public ComicTitleParserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

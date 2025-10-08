package org.comicVaultBackend.exceptions;

public class ComicTitleParserException extends Exception {
    public ComicTitleParserException(String message) {
        super(message);
    }

    public ComicTitleParserException(String message, Throwable cause) {
        super(message, cause);
    }
}

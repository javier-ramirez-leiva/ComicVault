package org.comicVaultBackend.exceptions;

public class ComicScrapperUntreatedException extends Exception {
    public ComicScrapperUntreatedException(String message) {
        super(message);
    }

    public ComicScrapperUntreatedException(String message, Throwable cause) {
        super(message, cause);
    }
}

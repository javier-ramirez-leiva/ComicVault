package org.comicVaultBackend.exceptions;

public class ComicVineException extends Exception {
    public ComicVineException(String message) {
        super(message);
    }

    public ComicVineException(String message, Throwable cause) {
        super(message, cause);
    }
}

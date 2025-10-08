package org.comicVaultBackend.exceptions;

public class ComicMetaDataException extends Exception  {
    public ComicMetaDataException(String message) {
        super(message);
    }

    public ComicMetaDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.comicVaultBackend.exceptions;

public class DownloadException extends Exception{
    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}

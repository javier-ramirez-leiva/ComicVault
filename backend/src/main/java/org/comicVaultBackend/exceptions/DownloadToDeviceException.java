package org.comicVaultBackend.exceptions;

public class DownloadToDeviceException extends Exception {
    public DownloadToDeviceException(String message) {
        super(message);
    }

    public DownloadToDeviceException(String message, Throwable cause) {
        super(message, cause);
    }
}

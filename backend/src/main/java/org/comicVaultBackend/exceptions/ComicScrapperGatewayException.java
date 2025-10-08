package org.comicVaultBackend.exceptions;

public class ComicScrapperGatewayException extends Exception {
    public ComicScrapperGatewayException(String message) {
        super(message);
    }

    public ComicScrapperGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}

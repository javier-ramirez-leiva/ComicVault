package org.comicVaultBackend.exceptions;

public class ComicScrapperGatewayPageException extends Exception {
    public ComicScrapperGatewayPageException(String message) {
        super(message);
    }

    public ComicScrapperGatewayPageException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.comicVaultBackend.exceptions;


public class SlackNotifyException extends Exception {
    public SlackNotifyException(String message) {
        super(message);
    }

    public SlackNotifyException(String message, Throwable cause) {
        super(message, cause);
    }
}


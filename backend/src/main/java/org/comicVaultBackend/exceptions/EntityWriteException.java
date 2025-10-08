package org.comicVaultBackend.exceptions;

public class EntityWriteException extends Exception {
    public EntityWriteException(String message) {
        super(message);
    }

    public EntityWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}

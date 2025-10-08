package org.comicVaultBackend.exceptions;

public class ConfigurationArgumentException extends Exception {
    public ConfigurationArgumentException(String message) {
        super(message);
    }

    public ConfigurationArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}

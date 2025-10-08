package org.comicVaultBackend.exceptions;

public class ConfigurationValueException extends Exception {
    public ConfigurationValueException(String message) {
        super(message);
    }

    public ConfigurationValueException(String message, Throwable cause) {
        super(message, cause);
    }
}

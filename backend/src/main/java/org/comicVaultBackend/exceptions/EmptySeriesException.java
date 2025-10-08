package org.comicVaultBackend.exceptions;

public class EmptySeriesException extends Exception {

    public EmptySeriesException(String id) {
        super(String.format("Series with ID '%s' is empty", id));
    }
}

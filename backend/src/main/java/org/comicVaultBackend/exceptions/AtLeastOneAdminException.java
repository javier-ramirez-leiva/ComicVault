package org.comicVaultBackend.exceptions;

public class AtLeastOneAdminException extends Exception {
    public AtLeastOneAdminException() {
        super("There should be at least one admin user");
    }
}

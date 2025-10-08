package org.comicVaultBackend.exceptions;

public class ForbiddenAuthException extends Exception {

    public ForbiddenAuthException() {
        super("Forbidden access. Unsatisfied authentication login");
    }
}

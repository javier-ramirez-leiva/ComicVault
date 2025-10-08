package org.comicVaultBackend.exceptions;

public class EntityAlreadyExistringException extends Exception {

    public EntityAlreadyExistringException(String id, EntityNotFoundException.Entity entity) {
        super(String.format("%s already exists: %s", entity.name(), id));
    }
}

package org.comicVaultBackend.exceptions;

public class EntityNotFoundException extends Exception {

    public enum Entity {
        COMIC,
        COMIC_SEARCH,
        SERIES,
        USER,
        REFRESH_TOKEN,
        DEVICES,
        JOB,
    }

    public EntityNotFoundException(String id, Entity entity) {
        super(String.format("%s not found: %s", entity.name(), id));
    }
}
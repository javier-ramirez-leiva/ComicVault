package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.ComicDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.exceptions.EntityWriteException;

import java.util.List;
import java.util.Optional;

public interface ComicService {

    ComicEntity createComic(ComicEntity Comic);

    List<ComicEntity> listAll();

    List<ComicEntity> listOnGoing();

    Optional<ComicEntity> getComicByID(String comicID);

    Optional<ComicEntity> getcomicbyidGc(String comicidGc);

    void updateNonNullProperties(ComicDTO comicDto, ComicEntity comicEntity) throws IllegalAccessException, EntityWriteException;

    void deleteComicById(String ComicID);

    void save(ComicEntity comicEntity);

    void setPageStatus(ComicEntity comicEntity, int pageStatus);

    void setReadStatus(ComicEntity comicEntity, Boolean readStatus);

    /*Based on double pages, return the page of the query or the previous for double pages*/
    int fixDoublePageQuery(ComicEntity comicEntity, int page);
}

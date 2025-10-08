package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ComicSearchDetailsDTO extends ComicSearchDTO {
    private String description;
    private List<TagDTO> tags;

    public ComicSearchDetailsDTO(ComicSearchDTO comicSearchDTO) {
        this.idGc = comicSearchDTO.getIdGc();
        this.idGcIssue = comicSearchDTO.getIdGcIssue();
        this.title = comicSearchDTO.getTitle();
        this.link = comicSearchDTO.getLink();
        this.image = comicSearchDTO.getImage();
        this.category = comicSearchDTO.getCategory();
        this.year = comicSearchDTO.getYear();
        this.size = comicSearchDTO.getSize();
        this.downloadingStatus = comicSearchDTO.getDownloadingStatus();
        this.series = comicSearchDTO.getSeries();
        this.totalBytes = comicSearchDTO.getTotalBytes();
        this.currentBytes = comicSearchDTO.getCurrentBytes();
        this.totalComics = comicSearchDTO.getTotalComics();
        this.currentComic = comicSearchDTO.getCurrentComic();
    }
}

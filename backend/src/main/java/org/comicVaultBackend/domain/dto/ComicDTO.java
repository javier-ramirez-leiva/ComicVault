package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComicDTO {

    private String id;
    private String idGc;
    private String idGcIssue;
    private String description;
    private String title;
    private Integer issue;
    private String category;
    private String year;
    private String size;
    private String path;
    private Integer pages;
    private Integer pageStatus;
    private Boolean readStatus;
    private Date createdAt;
    private String link;
    private String seriesID;
    private String seriesTitle;
    private List<ProgressDTO> progress;
    private List<TagDTO> tags;
    private List<Integer> doublePages;
    private boolean doublePageCover;


}

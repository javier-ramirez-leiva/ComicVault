package org.comicVaultBackend.domain.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeriesDTO {

    private String id;
    private String title;
    private String year;
    private String category;
    private Date createdAt;
    @Getter(AccessLevel.NONE)
    private Integer totalIssues;
    @Getter(AccessLevel.NONE)
    private Boolean readStatus;
    @Getter(AccessLevel.NONE)
    private Integer readIssues;

    private List<ComicDTO> comics;

    public Integer getTotalIssues() {
        return comics != null ? comics.size() : 0;
    }

    public Boolean getReadStatus() {
        if (comics == null || comics.isEmpty()) {
            return false;
        }
        return comics.stream().allMatch(ComicDTO::getReadStatus);
    }

    public Integer getReadIssues() {
        if (comics == null || comics.isEmpty()) {
            return 0;
        }
        return (int) comics.stream().filter(ComicDTO::getReadStatus).count();
    }

}

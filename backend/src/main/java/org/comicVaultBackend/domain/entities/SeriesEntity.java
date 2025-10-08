package org.comicVaultBackend.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "series")

public class SeriesEntity {
    private static final Logger logger = LoggerFactory.getLogger(SeriesEntity.class);

    @Id
    private String seriesID;
    @NotNull(message = "Title cannot be null")
    @Size(min = 1, message = "Title cannot be empty")
    private String title;
    private String year;
    @NotNull(message = "Category cannot be null")
    @Size(min = 1, message = "Category cannot be empty")
    private String category;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("issue ASC")
    @Getter
    @Setter
    private List<ComicEntity> comics;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = new Date();
        if (modifiedAt == null) modifiedAt = new Date();

    }

    public Date getLastProgressDate() {
        Date date = null;
        for (ComicEntity comic : comics) {
            Date comicDate = comic.getProgressDate();
            if (comicDate != null && (date == null || comicDate.after(date))) {
                date = comicDate;
            }
        }
        return date;
    }

    //Safer method to add a comic. Careful the issue information may change on the original ref
    public void addComic(ComicEntity comicEntity) {
        boolean found = comics.stream()
                .anyMatch(comic -> Objects.equals(comic.getIssue(), comicEntity.getIssue()));
        //If the issue already exists, put it at the end hoping for the best
        if (found) {
            int maxIssue = comics.stream()
                    .map(ComicEntity::getIssue)
                    .max(Integer::compareTo)
                    .orElse(0);

            comicEntity.setIssue(maxIssue + 1);
        }
        comics.add(comicEntity);
        logger.info("Add comic ('" + comicEntity.getId() + "': " + comicEntity.getTitle() + ") in series ('" + seriesID + "': " + title + ")");
    }

}

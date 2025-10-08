package org.comicVaultBackend.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "comics")
public class ComicEntity {
    @Id
    private String id;
    private String idGc;
    private String idGcIssue;
    @Column(columnDefinition = "TEXT")
    private String description;
    @NotNull(message = "Title cannot be null")
    @Size(min = 1, message = "Title cannot be empty")
    private String title;
    @Min(value = 1, message = "Issue must be at least 1")
    private Integer issue;
    @NotNull(message = "Category cannot be null")
    @Size(min = 1, message = "Category cannot be empty")
    private String category;
    private String year;
    private String size;
    @NotNull(message = "Path cannot be null")
    @Size(min = 1, message = "Path cannot be empty")
    private String path;
    @Min(value = 1, message = "Pages must be at least 1")
    private int pages;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdAt", updatable = false)
    private Date createdAt;
    private String link;

    @ManyToOne
    @JoinColumn(name = "seriesID", nullable = false)
    @ToString.Exclude
    private SeriesEntity series;

    @OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("progress_date")
    @Getter
    @Setter
    private List<ProgressEntity> progress;

    private List<Integer> doublePages;

    private boolean doublePageCover;

    @ManyToMany
    @JoinTable(
            name = "comic_tag",
            joinColumns = @JoinColumn(name = "comic_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> tags;


    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = new Date();
        if (idGc == null) idGc = id;
        if (progress == null) progress = new ArrayList<ProgressEntity>();
    }

    public int getPageStatus() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<ProgressEntity> progressEntity = progress.stream()
                .filter(progress -> progress.getUser().getUsername().equals(username))
                .findFirst();
        return progressEntity.map(ProgressEntity::getPageStatus).orElse(0);
    }


    public boolean getReadStatus() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<ProgressEntity> progressEntity = progress.stream()
                .filter(progress -> progress.getUser().getUsername().equals(username))
                .findFirst();
        return progressEntity.map(ProgressEntity::getReadStatus).orElse(false);
    }

    public Date getProgressDate() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<ProgressEntity> progressEntity = progress.stream()
                .filter(progress -> progress.getUser().getUsername().equals(username))
                .findFirst();
        return progressEntity.map(ProgressEntity::getProgress_date).orElse(null);
    }

    public List<Integer> getPairPages(int page) {
        List<List<Integer>> listPairDoublePages = listPairDoublePages();
        return listPairDoublePages.stream()
                .filter(pair -> pair.contains(page))
                .findFirst()
                .orElse(Collections.emptyList());
    }

    List<List<Integer>> listPairDoublePages() {
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i <= pages; ++i) {
            if (doublePages.contains(i) || doublePages.contains(i + 1)) {
                result.add(List.of(i));
            } else if (i == 0 && !doublePageCover) {
                result.add(List.of(i));
            } else if (i == pages - 1) {
                result.add(List.of(i));
            } else {
                result.add(List.of(i, i + 1));
                ++i;
            }
        }
        return result;
    }
}

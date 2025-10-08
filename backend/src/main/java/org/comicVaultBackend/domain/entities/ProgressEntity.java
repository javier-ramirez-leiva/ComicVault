package org.comicVaultBackend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="progress")
public class ProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long automaticId;

    @Column(name = "progress_date")
    private Date progress_date;

    @ManyToOne
    @JoinColumn(name = "username", nullable = false)
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    @ToString.Exclude
    private ComicEntity comic;

    private Integer pageStatus;
    private Boolean readStatus;

    @PrePersist
    protected void onCreate() {
        if (progress_date == null) progress_date = new Date();
        if (readStatus == null) readStatus = false;
        if (pageStatus == null) pageStatus = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        progress_date = new Date(); // Update progress_date on every save
    }

}

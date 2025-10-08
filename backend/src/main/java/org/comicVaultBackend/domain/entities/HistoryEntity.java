package org.comicVaultBackend.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="history")
public class HistoryEntity {
    @Id
    private Long automaticId;

    @Column(name = "progress_date")
    private Date progress_date;

    @NotNull(message = "Username cannot be null")
    private String username;

    @NotNull(message = "Title cannot be empty")
    private String comicTitle;

    @NotNull(message = "ComicID cannot be empty")
    private String comicId;

    @NotNull(message = "Page status cannot be empty")
    private Integer pageStatus;

    @Min(value = 1, message = "Pages must be at least 1")
    private Integer pages;

    @NotNull(message = "Read status cannot be empty")
    private Boolean readStatus;

    @NotNull(message = "Alive status cannot be empty")
    private Boolean alive;

}

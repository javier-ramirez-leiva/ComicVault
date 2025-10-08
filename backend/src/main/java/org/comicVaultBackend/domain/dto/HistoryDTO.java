package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryDTO {
    private Date progress_date;
    private String username;
    private String comicTitle;
    private String comicId;
    private Integer pageStatus;
    private Integer pages;
    private Boolean readStatus;
    private Boolean alive;
}

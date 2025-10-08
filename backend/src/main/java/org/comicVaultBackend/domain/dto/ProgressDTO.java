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
public class ProgressDTO {
    private Date progress_date;
    private String username;
    private String id;
    private Integer pageStatus;
    private Boolean readStatus;
}

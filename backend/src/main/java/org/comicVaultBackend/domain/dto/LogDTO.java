package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.comicVaultBackend.domain.entities.LogEntity;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class LogDTO {
    private Long logId;
    private Date timeStamp;
    private LogEntity.LogSeverity severity;
    private String severityMessage;
    private String message;
    private String details;
    private String username;
    @Builder.Default
    private boolean messageHref = false;
    @Builder.Default
    private String comicId = null;
    @Builder.Default
    private String seriesId = null;
    @Builder.Default
    private Long jobId = null;
}

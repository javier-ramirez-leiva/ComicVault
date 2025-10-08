package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.comicVaultBackend.domain.entities.JobEntity;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class JobDTO {
    private Long jobId;
    private Date timeStamp;
    private String duration;
    private String username;
    private List<Long> logsIds;
    private JobEntity.Type type;
    private JobEntity.STATUS status;
}

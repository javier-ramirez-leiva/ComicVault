package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DownloadingStatusDTO {
    protected String downloadingStatus;
    protected Long totalBytes;
    protected Long currentBytes;
}

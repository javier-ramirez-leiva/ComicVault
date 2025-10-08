package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DownloadRequestDTO {
    public ComicSearchDetailsDTO comicSearchDetails;
    public DownloadLinkDTO downloadLink;
}

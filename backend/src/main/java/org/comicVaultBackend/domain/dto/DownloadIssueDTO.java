package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadIssueDTO {
    private String description;
    private String idGcIssue;
    private String title;
    private List<DownloadLinkDTO> links;
}

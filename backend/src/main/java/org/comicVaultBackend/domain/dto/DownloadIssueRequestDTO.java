package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadIssueRequestDTO {
    private String description;
    private String idGcIssue;
    private String title;
    private DownloadLinkDTO link;
}
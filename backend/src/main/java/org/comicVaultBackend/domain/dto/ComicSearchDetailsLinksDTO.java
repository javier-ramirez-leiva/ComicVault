package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ComicSearchDetailsLinksDTO extends ComicSearchDetailsDTO {
    private List<DownloadIssueDTO> downloadIssues;
    private List<TagDTO> tags;
    private TagDTO mainTag;
}

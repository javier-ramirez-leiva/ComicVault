package org.comicVaultBackend.domain.regular;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComicTitle {
    private String series;
    private int issueNumber;
    private int volumeNumber;
    private String fileName;

}
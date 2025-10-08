package org.comicVaultBackend.domain.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter

public class JDownloaderConfigurationDTO {
    @Builder.Default
    private String jDownloaderCrawljobPath="";
    @Builder.Default
    private String jDownloaderOutputPath="";
    @Builder.Default
    private boolean deleteFolderOutputFolder=false;
}

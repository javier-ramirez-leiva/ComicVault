package org.comicVaultBackend.domain.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class ConfigurationDTO {
    String downloadRoot;
    boolean scanArchives;
    boolean deleteArchives;
    boolean generateNavigationThumbnails;
    SlackConfigurationDTO slackConfiguration;
    String comicVineAPIKey;
    JDownloaderConfigurationDTO jDownloaderConfiguration;
    String getComicsBaseUrl;
}

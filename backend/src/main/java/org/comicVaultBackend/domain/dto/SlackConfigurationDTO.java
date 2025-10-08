package org.comicVaultBackend.domain.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter

public class SlackConfigurationDTO {
    @Builder.Default
    private String slackWebHook = "";
    @Builder.Default
    private String comicVaultBaseUrl = "";
    @Builder.Default
    private boolean enableNotifications = false;
}

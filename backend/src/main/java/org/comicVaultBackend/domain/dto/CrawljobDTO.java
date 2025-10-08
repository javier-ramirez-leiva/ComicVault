package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CrawljobDTO {
        @Builder.Default
        private String enabled =  "TRUE";
        private String text;
        private String packageName;
        @Builder.Default
        private String autoConfirm = "TRUE";
        @Builder.Default
        private String autoStart = "TRUE";
        @Builder.Default
        private String extractAfterDownload = "TRUE";
        @Builder.Default
        private String forcedStart = "TRUE";
        private String downloadFolder;
        @Builder.Default
        private boolean overwritePackagizerEnabled=false;

}

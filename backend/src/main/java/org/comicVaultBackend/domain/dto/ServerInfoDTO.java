package org.comicVaultBackend.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerInfoDTO {

    String branch;
    String commitId;
    String commitTime;
    String commitMessage;
    String schemaVersion;
    String apiVersion;
}

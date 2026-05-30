package org.comicVaultBackend.domain.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter

public class GetComicsConfigurationDTO {
    String baseUrl;
    double requestsPerSecond;
}

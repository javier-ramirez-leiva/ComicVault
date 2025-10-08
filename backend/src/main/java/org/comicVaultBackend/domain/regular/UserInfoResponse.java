package org.comicVaultBackend.domain.regular;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserInfoResponse {
    private String username;
    private Role role;
    private String color;

}

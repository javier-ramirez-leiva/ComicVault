package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.comicVaultBackend.domain.regular.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class RegisterRequestDTO {
    private String username;
    private String password;
    private Role role;
    private String color;
}

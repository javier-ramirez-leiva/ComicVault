package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.comicVaultBackend.domain.regular.Role;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserDTO {
    private String username;
    private String password;
    private String color;
    @Builder.Default
    private boolean enabled = true;
    private Role role;
    private Date createdAt;
    private List<ProgressDTO> progress;
}

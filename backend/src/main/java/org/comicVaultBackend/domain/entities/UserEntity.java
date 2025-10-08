package org.comicVaultBackend.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.comicVaultBackend.domain.regular.Role;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private boolean enabled;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("progress_date")
    @Getter
    @Setter
    private List<ProgressEntity> progress;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = new Date();
    }

}

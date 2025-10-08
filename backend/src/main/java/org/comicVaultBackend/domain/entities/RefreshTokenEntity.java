package org.comicVaultBackend.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "refreshToken")
public class RefreshTokenEntity {
    @Id
    @Column(unique = true)
    private String id;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Date expiryDate;

    @Column(unique = true, nullable = false)
    private String deviceId;

    private String userAgent;
    private String os;
    private String browser;
    private String device;
    private String osVersion;
    private String browserVersion;
    private String orientation;
    private String ip;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) createdAt = new Date();
        deviceId = UUID.randomUUID().toString();
    }
}

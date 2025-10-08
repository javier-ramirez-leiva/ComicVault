package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoggedDeviceInfoDTO {
    private String id;
    private String username;

    private String userAgent;
    private String os;
    private String browser;
    private String device;
    private String osVersion;
    private String browserVersion;
    private String orientation;
    private Date createdAt;
    private String ip;
}

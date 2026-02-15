package org.comicVaultBackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ExceptionDTO {
    private Long entityId;
    private Date timeStamp;
    private String type;
    private String message;
    private List<String> details;
}

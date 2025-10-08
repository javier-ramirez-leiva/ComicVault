package org.comicVaultBackend.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "log")
public class LogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;


    @Column(nullable = false)
    private Date timeStamp;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String severityMessage;

    @Column(nullable = false)
    private String username;

    private String message;
    private String details;
    private boolean messageHref;
    @Column(nullable = true)
    private String comicId;
    @Column(nullable = true)
    private String seriesId;

    private Long jobId;

    @PrePersist
    protected void onCreate() {
        if (timeStamp == null) timeStamp = new Date();
    }
}

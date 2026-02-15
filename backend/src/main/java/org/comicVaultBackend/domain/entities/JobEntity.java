package org.comicVaultBackend.domain.entities;

import jakarta.persistence.*;
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
@Entity
@Table(name = "job")
public class JobEntity {
    public enum Type {
        DOWNLOAD,
        DOWNLOAD_LIST,
        DELETE,
        SCAN_LIB,
        CLEAN_LIB,
        DELETE_LIB
    }

    public enum Status {
        NONE,
        ON_GOING,
        COMPLETED,
        ERROR,
        INTERRUPTED,
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false)
    private Date timeStamp;

    private String duration;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private Status status;

    private List<Long> logsIds;
}

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
@Table(name = "exception")
public class ExceptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entityId;

    @Column(nullable = false)
    private Date timeStamp;

    String message;

    List<String> details;
}
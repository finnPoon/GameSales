package com.vanguard.vanguardapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class CsvImportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ImportStatus status = ImportStatus.PENDING;

    private int totalRows = 0;

    private int importedRows = 0;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String createdBy;

    private String updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Getters and Setters

    public enum ImportStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}

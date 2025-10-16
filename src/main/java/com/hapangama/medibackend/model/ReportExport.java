package com.hapangama.medibackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_exports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportExport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String format;

    @Column(nullable = false)
    private LocalDateTime exportedAt;

    @Column(length = 2000)
    private String filterParameters;

    private String fileName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExportStatus status;

    public enum ExportStatus {
        COMPLETED,
        FAILED
    }
}

package com.king.eschool.Modules.Academique.Models;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.king.eschool.Modules.Academique.Enum.AcademicYearStatus;

@Entity
@Table(name = "academic_years", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"school_id", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 50)
    private String name; // Ex: "2025-2026"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_current")
    @Builder.Default
    private boolean isCurrent = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademicYearStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

package com.king.eschool.Modules.Academique.Models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import com.king.eschool.Modules.Academique.Enum.AcademicTermStatus;

@Entity
@Table(
    name = "academic_terms",
    indexes = {
        @Index(name = "idx_term_school", columnList = "school_id"),
        @Index(name = "idx_term_year", columnList = "academic_year_id"),
        @Index(
            name = "idx_term_school_year",
            columnList = "school_id, academic_year_id"
        )
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_term_school_year_code",
            columnNames = {
                "school_id",
                "academic_year_id",
                "code"
            }
        )
    }
)
public class AcademicTerm {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * École propriétaire.
     */
    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /**
     * Année scolaire propriétaire.
     */
    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    /**
     * TRIMESTER ou SEMESTER.
     */
    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 30)
    private AcademicTermStatus status;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public AcademicTerm() {
    }

    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public AcademicTermStatus getStatus() {
        return status;
    }

    public void setStatus(AcademicTermStatus status) {
        this.status = status;
    }
}
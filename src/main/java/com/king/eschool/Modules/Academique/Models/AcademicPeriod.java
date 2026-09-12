package com.king.eschool.Modules.Academique.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

import com.king.eschool.Modules.Academique.Enum.AcademicPeriodStatus;

@Entity
@Table(name = "academic_periods")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AcademicPeriod {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "academic_year_id", nullable = false)
    // private AcademicYear academicYear;

    private UUID academicYearId;
    private  UUID academicTermId;
    private  UUID schoolId;

    @Column(nullable = false, length = 100)
    private String name; // Ex: Premier Trimestre

    @Column(nullable = false, length = 30)
    private String code; // Ex: T1

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademicPeriodStatus status;




}

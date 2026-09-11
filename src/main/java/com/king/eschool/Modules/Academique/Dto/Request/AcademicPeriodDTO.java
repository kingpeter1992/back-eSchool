package com.king.eschool.Modules.Academique.Dto.Request;

import java.time.LocalDate;
import java.util.UUID;

import com.king.eschool.Modules.Academique.Enum.AcademicPeriodStatus;

public record AcademicPeriodDTO(
    String id,
    UUID academicYearId,
    String name,
    String code,
    LocalDate startDate,
    LocalDate endDate,
    AcademicPeriodStatus status
) {

    public AcademicPeriodDTO(UUID academicYearId, String name, String code, LocalDate startDate, LocalDate endDate) {
        this(null, academicYearId, name, code, startDate, endDate, AcademicPeriodStatus.UPCOMING);
    }
}
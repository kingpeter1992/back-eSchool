package com.king.eschool.Modules.Academique.Dto.Request;

import java.time.LocalDate;
import java.util.UUID;

import com.king.eschool.Modules.Academique.Enum.AcademicYearStatus;

public record AcademicYearDTO(
    String id,
    String schoolId,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    AcademicYearStatus status
) {

    public AcademicYearDTO(UUID id2, UUID schoolId2, String name2, LocalDate startDate2, LocalDate endDate2,
            AcademicYearStatus status2) {
        this(id2.toString(), schoolId2.toString(), name2, startDate2, endDate2, status2);
            }}

package com.king.eschool.Modules.Academique.Dto.Request;

import java.time.LocalDate;
import java.util.UUID;

import com.king.eschool.Modules.Academique.Enum.AcademicYearStatus;

import lombok.*;
// DTO pour la création d'une classe (UC-CLS-001)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class AcademicYearDTO{
    UUID id;
    UUID schoolId;
    String name;
    LocalDate startDate;
    LocalDate endDate;
    AcademicYearStatus status;
}
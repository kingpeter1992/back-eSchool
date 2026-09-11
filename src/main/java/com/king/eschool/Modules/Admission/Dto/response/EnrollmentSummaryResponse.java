package com.king.eschool.Modules.Admission.Dto.response;

import java.time.LocalDate;

import lombok.*;
import java.util.UUID;

import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentSummaryResponse {
     private UUID id;

    private String registrationNo;

    private UUID studentId;

    private String studentName;

    private UUID campusId;

    private String campusName;

    private UUID classId;

    private String className;

    private UUID academicYearId;

    private String academicYearName;

    private EnrollmentStatus status;

    private LocalDate admissionDate;
}

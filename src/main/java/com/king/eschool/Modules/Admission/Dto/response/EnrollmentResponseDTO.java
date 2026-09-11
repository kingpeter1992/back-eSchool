package com.king.eschool.Modules.Admission.Dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;

@Data
@Builder
public class EnrollmentResponseDTO {
   private UUID id;

    private UUID schoolId;

    private String schoolName;

    private UUID campusId;

    private String campusName;

    private UUID studentId;

    private String studentName;

    private String studentEmail;

    private UUID classId;

    private String className;

    private UUID academicYearId;

    private String academicYearName;

    private String registrationNo;

    private EnrollmentStatus status;

    private LocalDate admissionDate;

    private LocalDate createdAt;

    private LocalDate updatedAt;
}
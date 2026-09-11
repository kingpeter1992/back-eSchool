package com.king.eschool.Modules.Admission.Dto.request;

import java.util.UUID;

import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;

import lombok.Data;

@Data
public class UpdateEnrollmentRequest {

    private UUID campusId;
    private UUID classId;
    private UUID academicYearId;
    private EnrollmentStatus status;
}

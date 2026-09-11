package com.king.eschool.Modules.Admission.Dto.request;

import java.util.UUID;

import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateEnrollmentStatusRequestDTO {
    @NotNull(message = "Le statut est obligatoire")
    private EnrollmentStatus status;

    private UUID classId;
    private String rejectionReason;
}
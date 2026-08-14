package com.king.eschool.Modules.School.Dto.request;

import com.king.eschool.Modules.School.Dto.SchoolStatus;

import jakarta.validation.constraints.NotNull;

public record SchoolStatusUpdateRequest(
        @NotNull(message = "Le statut est obligatoire")
        SchoolStatus status
) {}

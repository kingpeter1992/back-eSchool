package com.king.eschool.Modules.Admission.Dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AssignClassRequestDTO {
    @NotNull(message = "La classe est obligatoire")
    private UUID classId;
    @NotNull(message = "L'ID de l'inscription est obligatoire")
    UUID enrollmentId;
    UUID campusId;

    private boolean adminOverride = false;
    boolean overrideCapacity;
    String overrideReason;
}

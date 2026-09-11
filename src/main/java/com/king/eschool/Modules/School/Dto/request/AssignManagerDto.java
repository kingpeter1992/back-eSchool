package com.king.eschool.Modules.School.Dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignManagerDto {
    @NotNull(message = "L'ID du responsable est obligatoire")
    private UUID managerId;
}
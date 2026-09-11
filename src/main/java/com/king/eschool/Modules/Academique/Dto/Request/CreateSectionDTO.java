package com.king.eschool.Modules.Academique.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSectionDTO {
    @NotBlank(message = "Le cycle est obligatoire")
    private String cycleId;

    @NotBlank(message = "Le nom de la section est obligatoire")
    private String name;
}

package com.king.eschool.Modules.Academique.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOptionDTO {
    @NotBlank(message = "La section est obligatoire")
    private String sectionId;

    @NotBlank(message = "Le nom de l'option est obligatoire")
    private String name;

    @NotBlank(message = "Le code de l'option est obligatoire")
    private String code;
}
package com.king.eschool.Modules.Academique.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class CreateCycleRequest {
    @NotBlank(message = "L'ID de l'école est obligatoire")
    private String schoolId;

    @NotBlank(message = "Le nom du cycle est obligatoire")
    private String name;
}

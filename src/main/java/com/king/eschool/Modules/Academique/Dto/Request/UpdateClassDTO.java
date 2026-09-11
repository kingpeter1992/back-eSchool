package com.king.eschool.Modules.Academique.Dto.Request;


import com.king.eschool.Modules.Academique.Enum.ClassStatus;
import com.king.eschool.Modules.Academique.Models.ShiftType;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class UpdateClassDTO {

    @NotBlank(message = "Le nom de la classe est obligatoire")
    private String name;

    private Integer maxCapacity;

    private String mainTeacherId;

    private ClassStatus status;

    private String roomId; // Optionnel ou obligatoire selon vos contraintes
// Dans UpdateClassDTO
private ShiftType shift;
}
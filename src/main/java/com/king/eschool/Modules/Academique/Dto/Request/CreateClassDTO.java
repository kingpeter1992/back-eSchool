package com.king.eschool.Modules.Academique.Dto.Request;

import com.king.eschool.Modules.Academique.Models.ShiftType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO pour la création d'une classe (UC-CLS-001)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassDTO {

@NotBlank(message = "L'école est obligatoire")
    private String schoolId;

    @NotBlank(message = "Le campus est obligatoire")
    private String campusId;

    @NotBlank(message = "Le niveau d'études est obligatoire")
    private String levelId;

    @NotBlank(message = "Le nom de la classe est obligatoire")
    private String name;

    private Integer maxCapacity;
    private String mainTeacherId;
    private String roomId; // Optionnel ou obligatoire selon vos contraintes

    // Dans CreateClassDTO
private ShiftType shift;
}

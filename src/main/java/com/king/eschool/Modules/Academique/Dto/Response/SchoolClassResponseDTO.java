package com.king.eschool.Modules.Academique.Dto.Response;

import com.king.eschool.Modules.Academique.Enum.ClassStatus;
import com.king.eschool.Modules.Academique.Models.ShiftType;

import lombok.*;

// DTO pour la réponse HTTP de la classe
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassResponseDTO {

    private String id;
    private String schoolId;
    private String campusId;
    private String academicYearId;
    private String levelId;
    private String levelName;
    private String roomId;
    private String name;
    private Integer maxCapacity;
    private long currentEnrollment; // Nombre d'élèves actuellement inscrits
    private String mainTeacherId;
    private ClassStatus status;
    private ShiftType shift;
}
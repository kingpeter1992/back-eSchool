package com.king.eschool.Modules.Academique.Dto.Request;


import java.time.LocalDate;
import java.util.UUID;

import com.king.eschool.Modules.Academique.Enum.AcademicTermStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  AcademicTermDTO {
        UUID id;
        UUID academicYearId;
        UUID schoolId;
        String type;
        String name;
        String code;
        LocalDate startDate;
        LocalDate endDate;
        AcademicTermStatus status;

}
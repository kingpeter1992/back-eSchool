package com.king.eschool.Modules.Academique.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicCycleDTO {
    private String id;
    private String schoolId;
    private String name;
    private List<AcademicSectionDTO> sections;
    private List<AcademicLevelDTO> levels;
}
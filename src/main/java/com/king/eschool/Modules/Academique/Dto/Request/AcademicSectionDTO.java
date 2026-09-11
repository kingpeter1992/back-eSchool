package com.king.eschool.Modules.Academique.Dto.Request;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicSectionDTO {
    private String id;
    private String cycleId;
    private String name;
    private List<AcademicOptionDTO> options;
}
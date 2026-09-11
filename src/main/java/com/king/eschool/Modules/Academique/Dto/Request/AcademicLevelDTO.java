package com.king.eschool.Modules.Academique.Dto.Request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicLevelDTO {
    private String id;
    private String cycleId;
    private String optionId;
    private String name;
    private Integer numericOrder;
}
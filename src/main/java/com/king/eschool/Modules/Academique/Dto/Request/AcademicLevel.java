package com.king.eschool.Modules.Academique.Dto.Request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicLevel {
    private String cycleId;
    private String name;
    private Integer numericOrder;
}
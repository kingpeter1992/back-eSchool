package com.king.eschool.Modules.Academique.Dto.Request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicOptionDTO {
    private String id;
    private String sectionId;
    private String name;
    private String code;
}
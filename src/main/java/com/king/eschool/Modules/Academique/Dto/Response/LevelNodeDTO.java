package com.king.eschool.Modules.Academique.Dto.Response;

import lombok.*;

@Data 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LevelNodeDTO {
    private String id;
    private String name;
    private Integer numericOrder;
}
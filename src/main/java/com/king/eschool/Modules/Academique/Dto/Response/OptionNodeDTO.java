package com.king.eschool.Modules.Academique.Dto.Response;

import lombok.*;

@Data 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionNodeDTO {
    private String id;
    private String name;
    private String code;
}
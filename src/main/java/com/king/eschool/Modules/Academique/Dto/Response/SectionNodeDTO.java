package com.king.eschool.Modules.Academique.Dto.Response;

import java.util.List;

import lombok.*;

@Data 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionNodeDTO {
    private String id;
    private String name;
    private List<OptionNodeDTO> options;
}
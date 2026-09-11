package com.king.eschool.Modules.Academique.Dto.Response;

import java.util.List;

import lombok.*;

@Data 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CycleNodeDTO {
    private String id;
    private String name;
    private List<LevelNodeDTO> levels;
    private List<SectionNodeDTO> sections;
}

package com.king.eschool.Modules.School.Dto.reponse;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingResponseDto {
    private UUID id;
    private String name;
    private String code;
    private UUID campusId;
    private Long floors;
    private List<RoomResponseDto> rooms; // Ajouter ce champ
    private LocalDateTime createdAt;
}
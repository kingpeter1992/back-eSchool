package com.king.eschool.Modules.School.Dto.reponse;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {
    private UUID id;
    private String name;
    private Integer capacity;
    private List<EquipmentResponseDto> equipments;
    private UUID buildingId;
}

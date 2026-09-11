package com.king.eschool.Modules.School.Dto.reponse;


import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentResponseDto {
private UUID id; // 👈 Identifiant unique exposé dans le DTO
    private String name;
    private Integer quantity;
    private String state;
}

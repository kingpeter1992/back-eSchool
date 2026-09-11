package com.king.eschool.Modules.School.Dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequestDto {
    private UUID id; // Null pour création, renseigné pour modification
    @NotBlank(message = "Le nom de l'équipement est obligatoire")
    private String name;
    private Integer quantity;
    private String state;
    private String roomId;
}

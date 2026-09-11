package com.king.eschool.Modules.School.Dto.request;

import java.util.List;

import com.king.eschool.Modules.School.Models.Equipment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequestDto {
    @NotBlank private String name;
    @NotNull @Min(1) private Integer capacity;
    private List<Equipment> equipments;
}

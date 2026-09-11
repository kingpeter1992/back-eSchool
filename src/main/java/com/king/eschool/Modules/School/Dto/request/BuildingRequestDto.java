package com.king.eschool.Modules.School.Dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuildingRequestDto {
    @NotBlank 
    private String name;
    private String code;
    private Long floors;
}
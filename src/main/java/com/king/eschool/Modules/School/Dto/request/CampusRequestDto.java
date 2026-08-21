package com.king.eschool.Modules.School.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CampusRequestDto {

    @NotNull(message = "L'ID de l'école est obligatoire (RG-CAM-001)")
    private UUID schoolId;

    @NotBlank(message = "Le nom du campus est obligatoire")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Le code du campus est obligatoire")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    private String city;

    private String country;
    private String phone;
}
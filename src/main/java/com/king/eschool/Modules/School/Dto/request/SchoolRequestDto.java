package com.king.eschool.Modules.School.Dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter @Getter
public class SchoolRequestDto {
    @NotBlank(message = "Le nom de l'école est obligatoire")
    private String name;
    @NotBlank(message = "L'email institutionnel est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    private String phone;
    private String currency;
    private String timezone;
    private String domain;
}
package com.king.eschool.Modules.Utilisateurs.Dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompleteActivationDto {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    // Métadonnées optionnelles / spécifiques selon le rôle
    private String phone;
    private String occupation;        // Pour PARENT
    private String birthDate;         // Pour ELEVE
    private String matricule;         // Pour DIRECTEUR / ADMIN
}
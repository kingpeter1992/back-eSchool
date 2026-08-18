package com.king.eschool.Modules.Utilisateurs.Dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivationContextDto {
    private String email;
    private String firstName;
    private String lastName;
    private String role; // ROLE_SUPER_ADMIN, ROLE_ADMIN, ROLE_DIRECTEUR, ROLE_PARENT, ROLE_ELEVE
    private UUID schoolId;
}
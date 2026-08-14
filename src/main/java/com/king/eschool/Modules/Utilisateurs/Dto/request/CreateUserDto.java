package com.king.eschool.Modules.Utilisateurs.Dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class CreateUserDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email @NotBlank
    private String email;
    private String phone;
    private UUID schoolId;
    private UUID campusId;
    private Set<String> roleSlugs; // Ex: ["ROLE_SCHOOL_ADMIN"]
}

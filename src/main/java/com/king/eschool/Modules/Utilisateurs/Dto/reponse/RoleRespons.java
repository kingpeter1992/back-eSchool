package com.king.eschool.Modules.Utilisateurs.Dto.reponse;

import java.util.UUID;

import com.king.eschool.Modules.Utilisateurs.Models.Role;

// DTO
public record RoleRespons(
    UUID id,
    String name,
    String slug,
    String description
) {
    public static RoleRespons fromEntity(Role role) {
        return new RoleRespons(role.getId(), role.getName(), role.getSlug(), role.getDescription());
    }
}

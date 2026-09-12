package com.king.eschool.Modules.Utilisateurs.Dto.reponse;

import java.util.UUID;

import com.king.eschool.Modules.Utilisateurs.Models.Role;

// DTO
public record RoleResponse(
    UUID id,
    String name,
    String slug,
    String description
) {
    public static RoleResponse fromEntity(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getSlug(), role.getDescription());
    }
}

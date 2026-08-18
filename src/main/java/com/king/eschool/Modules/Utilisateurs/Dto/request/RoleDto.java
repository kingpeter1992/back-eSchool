package com.king.eschool.Modules.Utilisateurs.Dto.request;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class RoleDto {

private UUID id;
    private String name;
    private String slug;
    private String description;
    private boolean system = false;
    
    // 🟢 Remplacer List<Permission> par List<UUID>
    private List<UUID> permissionIds;
}
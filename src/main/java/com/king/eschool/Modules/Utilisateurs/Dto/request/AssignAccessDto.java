package com.king.eschool.Modules.Utilisateurs.Dto.request;


import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class AssignAccessDto {
    private UUID userId;
    private Set<UUID> roleIds;
    private Set<UUID> permissionIds;
}
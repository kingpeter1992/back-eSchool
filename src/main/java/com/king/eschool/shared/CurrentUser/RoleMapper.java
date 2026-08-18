package com.king.eschool.shared.CurrentUser;


import org.springframework.stereotype.Component;

import com.king.eschool.Modules.Utilisateurs.Dto.request.RoleDto;
import com.king.eschool.Modules.Utilisateurs.Models.Role;

@Component
public class RoleMapper {
public RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        }

        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setSystem(role.isSystem());

      if (role.getPermissions() != null) {
    dto.setPermissionIds(
        role.getPermissions().stream()
            .map(permission -> permission.getId())
            .toList()
    );
}

        return dto;
    }
}
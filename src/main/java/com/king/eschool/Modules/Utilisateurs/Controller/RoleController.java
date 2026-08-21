package com.king.eschool.Modules.Utilisateurs.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.king.eschool.Modules.Utilisateurs.Dto.request.RoleDto;
import com.king.eschool.Modules.Utilisateurs.ServiceImplement.RoleService;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    // 🟢 Remplacement de 'private' par 'public'
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

@GetMapping
//@PreAuthorize("hasAuthority('role:read.all') or hasRole('SUPER_ADMIN')") // 🟢 Autorise le Super Admin
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE'))")
public ResponseEntity<List<RoleDto>> getRoles() {
    List<RoleDto> roles = roleService.getRoles();
    return ResponseEntity.ok(roles);
}

@PutMapping("/{id}/permissions")
//@PreAuthorize("hasAuthority('role:update') or hasRole('SUPER_ADMIN')") // 🟢 Autorise le Super Admin
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE'))")
public ResponseEntity<Void> updateRolePermissions(
        @PathVariable UUID id,
        @RequestBody List<UUID> permissionIds) {
    
    roleService.updateRolePermissions(id, permissionIds);
    return ResponseEntity.noContent().build();
}
}
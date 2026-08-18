package com.king.eschool.Modules.Utilisateurs.ServiceImplement;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import com.king.eschool.Modules.Utilisateurs.Dto.request.RoleDto;
import com.king.eschool.Modules.Utilisateurs.Models.Permission;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Repository.PermissionRepository;
import com.king.eschool.Modules.Utilisateurs.Repository.RoleRepository;
import com.king.eschool.shared.CurrentUser.RoleMapper;
import com.king.eschool.shared.CurrentUser.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SecurityUtils securityUtils;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public List<RoleDto> getRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @CacheEvict(value = "user-permissions", allEntries = true)
    public void updateRolePermissions(UUID roleId, List<UUID> permissionIds) {
        
        // 1. Déclenche AccessDeniedException -> Capturé en HTTP 403
        if (!securityUtils.isSuperAdmin()) {
            throw new AccessDeniedException("Seul le Super Administrateur peut modifier la matrice des rôles.");
        }

        // 2. Déclenche ResourceNotFoundException -> Capturé en HTTP 404
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceAccessException("Rôle non trouvé avec l'ID : " + roleId));

        // 3. Déclenche IllegalStateException -> Capturé en HTTP 409 (Conflit)
        if (role.isSystem()) {
            throw new IllegalStateException("Les rôles système par défaut ne peuvent pas être modifiés.");
        }

        Set<Permission> newPermissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        
        // 4. Déclenche IllegalArgumentException si la liste contient des IDs invalides -> HTTP 400
        if (newPermissions.size() != permissionIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs permissions fournies sont introuvables.");
        }

        role.setPermissions(newPermissions);
        roleRepository.save(role);
    }
}
package com.king.eschool.Modules.Utilisateurs.ServiceImplement;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Modules.Utilisateurs.Dto.request.PermissionDto;
import com.king.eschool.Modules.Utilisateurs.Models.Permission;
import com.king.eschool.Modules.Utilisateurs.Repository.PermissionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;


    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private PermissionDto mapToDto(Permission permission) {
        return PermissionDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .slug(permission.getSlug())
                .build();
    }
}

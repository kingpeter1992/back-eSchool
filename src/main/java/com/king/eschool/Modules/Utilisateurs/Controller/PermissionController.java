package com.king.eschool.Modules.Utilisateurs.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.king.eschool.Modules.Utilisateurs.Dto.request.PermissionDto;
import com.king.eschool.Modules.Utilisateurs.ServiceImplement.PermissionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;


    @GetMapping
    //@PreAuthorize("hasAuthority('permission:read.all') or hasRole('SUPER_ADMIN')")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE'))")
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }
}
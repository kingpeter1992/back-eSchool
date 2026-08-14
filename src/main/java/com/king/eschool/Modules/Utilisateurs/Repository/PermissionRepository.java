package com.king.eschool.Modules.Utilisateurs.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.Utilisateurs.Models.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findBySlug(String slug);

        
}

package com.king.eschool.Modules.Utilisateurs.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.Utilisateurs.Models.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    Optional<Role> findBySlug(String slug);

}
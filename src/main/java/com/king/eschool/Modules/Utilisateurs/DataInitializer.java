package com.king.eschool.Modules.Utilisateurs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.king.eschool.Modules.Utilisateurs.Models.Permission;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.Models.User.UserStatus;
import com.king.eschool.Modules.Utilisateurs.Repository.PermissionRepository;
import com.king.eschool.Modules.Utilisateurs.Repository.RoleRepository;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PermissionRepository permissionRepository;
        private final PasswordEncoder passwordEncoder;

        public DataInitializer(UserRepository userRepository,
                        RoleRepository roleRepository,
                        PermissionRepository permissionRepository,
                        PasswordEncoder passwordEncoder) {
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.permissionRepository = permissionRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        public void run(String... args) throws Exception {

                // ==========================
                // 1. Création des permissions de base (exemples)
                // ==========================
                Permission pUserCreate = permissionRepository.findBySlug("user:create")
                                .orElseGet(() -> permissionRepository.save(
                                                Permission.builder()
                                                                .name("Créer un utilisateur")
                                                                .slug("user:create")
                                                                .code("USER_CREATE")
                                                                .build()));

                Permission pSchoolCreate = permissionRepository.findBySlug("school:create")
                                .orElseGet(() -> permissionRepository.save(
                                                Permission.builder()
                                                                .name("Créer une école")
                                                                .slug("school:create")
                                                                .code("SCHOOL_CREATE")
                                                                .build()));

                // Récupération dynamique de TOUTES les permissions existantes en base
                Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());


                // ==========================
                // 2. Création des rôles
                // ==========================

                // Rôle SUPER_ADMIN (On lui attribue TOUTES les permissions par défaut)
                Role superAdminRole = roleRepository.findBySlug("ROLE_SUPER_ADMIN").orElseGet(() -> {
                        return roleRepository.save(
                                        Role.builder()
                                                        .name("Super Administrateur")
                                                        .slug("ROLE_SUPER_ADMIN")
                                                        .system(true)
                                                        .permissions(allPermissions) // <-- Toutes les permissions
                                                        .build());
                });

                // Rôle ADMIN_ECOLE
                if (roleRepository.findBySlug("ROLE_ADMIN_ECOLE").isEmpty()) {
                        Set<Permission> schoolAdminPermissions = new HashSet<>(Arrays.asList(pUserCreate));
                        roleRepository.save(
                                        Role.builder()
                                                        .name("Administrateur d'École / Directeur")
                                                        .slug("ROLE_ADMIN_ECOLE")
                                                        .system(true)
                                                        .permissions(schoolAdminPermissions)
                                                        .build());
                }

                // Rôle ENSEIGNANT
                if (roleRepository.findBySlug("ROLE_ENSEIGNANT").isEmpty()) {
                        roleRepository.save(
                                        Role.builder()
                                                        .name("Enseignant")
                                                        .slug("ROLE_ENSEIGNANT")
                                                        .system(true)
                                                        .build());
                }

                // Rôle ELEVE
                if (roleRepository.findBySlug("ROLE_ELEVE").isEmpty()) {
                        roleRepository.save(
                                        Role.builder()
                                                        .name("Élève")
                                                        .slug("ROLE_ELEVE")
                                                        .system(true)
                                                        .build());
                }

                // Rôle PARENT
                if (roleRepository.findBySlug("ROLE_PARENT").isEmpty()) {
                        roleRepository.save(
                                        Role.builder()
                                                        .name("Parent")
                                                        .slug("ROLE_PARENT")
                                                        .system(true)
                                                        .build());
                }


                // ==========================
                // 3. Création du Super Admin (Avec TOUS les rôles)
                // ==========================
                if (!userRepository.existsByUsername("superadmin")) {

                        // Récupération de TOUS les rôles de la base de données
                        Set<Role> allRoles = new HashSet<>(roleRepository.findAll());

                        User defaultSuperAdmin = User.builder()
                                        .username("superadmin")
                                        .email("kingkapeta@gmail.com")
                                        .firstName("Super")
                                        .lastName("Admin")
                                        .status(UserStatus.ACTIVE)                                        
                                        .passwordHash(passwordEncoder.encode("SuperAdmin2026!"))
                                        .active(true)
                                        .roles(allRoles) // <-- Assigne TOUS les rôles d'un coup
                                        .build();

                        userRepository.save(defaultSuperAdmin);

                        System.out.println(
                                        "✅ [SÉCURITÉ] Compte Super Admin créé avec succès avec TOUS les rôles et permissions !");
                }
        }
}
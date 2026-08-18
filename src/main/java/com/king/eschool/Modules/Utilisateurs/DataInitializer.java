package com.king.eschool.Modules.Utilisateurs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Modules.Utilisateurs.Models.Permission;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.Models.User.UserStatus;
import com.king.eschool.Modules.Utilisateurs.Repository.PermissionRepository;
import com.king.eschool.Modules.Utilisateurs.Repository.RoleRepository;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
        @Transactional // 🟢 Indispensable pour garder la session Hibernate ouverte lors de
                       // l'enregistrement des relations
        public void run(String... args) throws Exception {

                // ==========================================
                // 1. Création / Mise à jour des permissions
                // ==========================================
                Permission pUserCreate = createPermissionIfNotFound("Créer un utilisateur", "user:create",
                                "USER_CREATE");
                Permission pSchoolCreate = createPermissionIfNotFound("Créer une école", "school:create",
                                "SCHOOL_CREATE");
                Permission pSchoolRead = createPermissionIfNotFound("Lire les écoles", "school:read.all",
                                "SCHOOL_READ_ALL");

                // Charger TOUTES les permissions existantes
                Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

                // ==========================================
                // 2. Création / Mise à jour des Rôles
                // ==========================================

                // Rôle SUPER_ADMIN (Recherche ou création)
                Role superAdminRole = roleRepository.findBySlug("ROLE_SUPER_ADMIN")
                                .orElseGet(() -> roleRepository.save(
                                                Role.builder()
                                                                .name("Super Administrateur")
                                                                .slug("ROLE_SUPER_ADMIN")
                                                                .system(true)
                                                                .build()));

                // 🟢 S'assurer que SUPER_ADMIN possède TOUJOURS toutes les permissions
                superAdminRole.setPermissions(allPermissions);
                roleRepository.save(superAdminRole);

                // Rôle ADMIN_ECOLE
                if (roleRepository.findBySlug("ROLE_ADMIN_ECOLE").isEmpty()) {
                        roleRepository.save(
                                        Role.builder()
                                                        .name("Administrateur d'École / Directeur")
                                                        .slug("ROLE_ADMIN_ECOLE")
                                                        .system(true)
                                                        .permissions(Set.of(pUserCreate, pSchoolRead))
                                                        .build());
                }

                // Rôle ENSEIGNANT
                createRoleIfNotFound("Enseignant", "ROLE_ENSEIGNANT");

                // Rôle ELEVE
                createRoleIfNotFound("Élève", "ROLE_ELEVE");

                // Rôle PARENT
                createRoleIfNotFound("Parent", "ROLE_PARENT");

                // ==========================================
                // 3. Création du compte Super Admin
                // ==========================================
                if (!userRepository.existsByUsername("superadmin")) {

                        User defaultSuperAdmin = User.builder()
                                        .username("superadmin")
                                        .email("kingkapeta@gmail.com")
                                        .firstName("Super")
                                        .lastName("Admin")
                                        .status(UserStatus.ACTIVE)
                                        .passwordHash(passwordEncoder.encode("SuperAdmin2026!"))
                                        .active(true)
                                        .roles(Set.of(superAdminRole)) // 🟢 Uniquement le rôle SUPER_ADMIN !
                                        .build();

                        userRepository.save(defaultSuperAdmin);

//Permission p1 = new Permission("Consulter les Rôles", "role:read.all", "Gestion des Rôles");
//Permission p2 = new Permission("Modifier les Rôles", "role:update", "Gestion des Rôles");
//permissionRepository.saveAll(List.of(p1, p2));

                        System.out.println(
                                        "✅ [SÉCURITÉ] Compte Super Admin créé avec succès avec le rôle ROLE_SUPER_ADMIN !");
                }
        }

        // 🛠️ Méthodes utilitaires pour garder le code propre :

        private Permission createPermissionIfNotFound(String name, String slug, String code) {
                return permissionRepository.findBySlug(slug)
                                .orElseGet(() -> permissionRepository.save(
                                                Permission.builder()
                                                                .name(name)
                                                                .slug(slug)
                                                                .code(code)
                                                                .build()));
        }

        private void createRoleIfNotFound(String name, String slug) {
                if (roleRepository.findBySlug(slug).isEmpty()) {
                        roleRepository.save(
                                        Role.builder()
                                                        .name(name)
                                                        .slug(slug)
                                                        .system(true)
                                                        .build());
                }
        }
}
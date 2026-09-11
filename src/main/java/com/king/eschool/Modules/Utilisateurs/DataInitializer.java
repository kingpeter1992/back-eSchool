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

import java.util.HashSet;
import java.util.Optional;
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
    @Transactional
    public void run(String... args) throws Exception {

        // ==========================================
        // 1. Création / Mise à jour des permissions
        // ==========================================
        Permission pUserCreate = createPermissionIfNotFound("Créer un utilisateur", "user:create", "USER_CREATE");
        Permission pSchoolCreate = createPermissionIfNotFound("Créer une école", "school:create", "SCHOOL_CREATE");
        Permission pSchoolRead = createPermissionIfNotFound("Lire les écoles", "school:read.all", "SCHOOL_READ_ALL");
        Permission pAdminSchoolUpdat = createPermissionIfNotFound("Mise à jour de campus", "campus:update", "SCHOOL_ADMIN_UPDATE");
        Permission pCampusCreate = createPermissionIfNotFound("Créer un campus", "campus:create", "CAMPUS_CREATE");
        Permission pCampusRead = createPermissionIfNotFound("Lire les campus", "campus:read.all", "SCHOOL_CAMPUS_READ_ALL");
        Permission pCampusUpdate = createPermissionIfNotFound("Update de campus", "campus:update", "CAMPUS_UPDATE");

        Permission pAcademicReadAll = createPermissionIfNotFound("Lire les années académiques", "academic-year:read.all", "ACADEMIC_YEAR_READ_ALL");
        Permission pAcademicCreate = createPermissionIfNotFound("Créer une année académique", "academic-year:create", "ACADEMIC_YEAR_CREATE");
        Permission pAcademicUpdate = createPermissionIfNotFound("Mettre à jour une année académique", "academic-year:update", "ACADEMIC_YEAR_UPDATE");
        Permission pAcademicDelete = createPermissionIfNotFound("Supprimer une année académique", "academic-year:delete", "ACADEMIC_YEAR_DELETE");

        Permission pAClasscreate = createPermissionIfNotFound("Créer une classe", "class:create", "CLASS_CREATE");
        Permission pAClassRead = createPermissionIfNotFound("Lire les classes", "class:read.all", "CLASS_READ_ALL");
        Permission pAClassUpdate = createPermissionIfNotFound("Mettre à jour une classe", "class:update", "CLASS_UPDATE");
        Permission pAClassDelete = createPermissionIfNotFound("Supprimer une classe", "class:delete", "CLASS_DELETE");

        Permission pACyclecreate = createPermissionIfNotFound("Créer un cycle", "academic_structure:create", "ACADEMIC_STRUCTURE_CREATE");
        Permission pACycleDelete = createPermissionIfNotFound("Supprimer une section", "academic_structure:delete", "ACADEMIC_STRUCTURE_DELETE");
        Permission pACycleUpdate = createPermissionIfNotFound("Mettre à jour une structure", "academic_structure:update", "ACADEMIC_STRUCTURE_UPDATE");

        Permission pEnrollmentCreate = createPermissionIfNotFound(
                        "Créer une inscription",
                        "enrollment:create",
                        "ENROLLMENT_CREATE");

        Permission pEnrollmentReadAll = createPermissionIfNotFound(
                        "Consulter toutes les inscriptions",
                        "enrollment:read.all",
                        "ENROLLMENT_READ_ALL");

        Permission pEnrollmentUpdate = createPermissionIfNotFound(
                        "Mettre à jour une inscription",
                        "enrollment:update",
                        "ENROLLMENT_UPDATE");

        Permission pEnrollmentDelete = createPermissionIfNotFound(
                        "Supprimer une inscription",
                        "enrollment:delete",
                        "ENROLLMENT_DELETE");



        // Récupérer TOUTES les permissions enregistrées
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

        // ==========================================
        // 2. Création / Mise à jour des Rôles
        // ==========================================

        // Rôle SUPER_ADMIN
        Role superAdminRole = roleRepository.findBySlug("ROLE_SUPER_ADMIN")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("Super Administrateur")
                                .slug("ROLE_SUPER_ADMIN")
                                .system(true)
                                .build()));

        // 🟢 Assigner TOUTES les permissions au rôle SUPER_ADMIN uniquement
        superAdminRole.setPermissions(allPermissions);
        roleRepository.save(superAdminRole);

        // Création des autres rôles applicatifs
        createRoleIfNotFound("Administrateur d'École / Directeur", "ROLE_ADMIN_ECOLE", Set.of(
                pUserCreate, pSchoolRead, pAcademicReadAll, pAcademicCreate, 
                pAcademicUpdate, pAcademicDelete, pAClasscreate, pAClassRead, 
                pAClassUpdate, pAClassDelete, pACyclecreate, pACycleDelete, pACycleUpdate
        ));

        createRoleIfNotFound("Enseignant", "ROLE_ENSEIGNANT", Set.of());
        createRoleIfNotFound("Élève", "ROLE_ELEVE", Set.of());
        createRoleIfNotFound("Parent", "ROLE_PARENT", Set.of());
        createRoleIfNotFound("Admin", "ROLE_ADMIN", Set.of());
        createRoleIfNotFound("User", "ROLE_USER", Set.of());

        // ==========================================
        // 3. Création / Mise à jour du Super Admin
        // ==========================================
        Optional<User> existingSuperAdmin = userRepository.findByEmail("kingkapeta@gmail.com");

        // Attribuer UNIQUEMENT le rôle SUPER_ADMIN à cet utilisateur
Set<Role> superAdminRoles = new HashSet<>(Set.of(superAdminRole));
        if (existingSuperAdmin.isPresent()) {
            User superAdmin = existingSuperAdmin.get();
            superAdmin.setRoles(superAdminRoles);
            userRepository.save(superAdmin);
            System.out.println("🔄 [SÉCURITÉ] Compte Super Admin mis à jour avec le rôle ROLE_SUPER_ADMIN et TOUTES les permissions !");
        } else {
            User defaultSuperAdmin = User.builder()
                    .username("superadmin")
                    .email("kingkapeta@gmail.com")
                    .firstName("Super")
                    .lastName("Admin")
                    .status(UserStatus.ACTIVE)
                    .passwordHash(passwordEncoder.encode("SuperAdmin2026!"))
                    .active(true)
                    .roles(superAdminRoles) // 🟢 Uniquement le rôle ROLE_SUPER_ADMIN
                    .build();

            userRepository.save(defaultSuperAdmin);
            System.out.println("✅ [SÉCURITÉ] Compte Super Admin créé avec le rôle ROLE_SUPER_ADMIN et TOUTES les permissions !");
        }
    }

    // 🛠️ Méthodes utilitaires

    private Permission createPermissionIfNotFound(String name, String slug, String code) {
        return permissionRepository.findBySlug(slug)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder()
                                .name(name)
                                .slug(slug)
                                .code(code)
                                .build()));
    }

    private void createRoleIfNotFound(String name, String slug, Set<Permission> permissions) {
        if (roleRepository.findBySlug(slug).isEmpty()) {
            roleRepository.save(
                    Role.builder()
                            .name(name)
                            .slug(slug)
                            .system(true)
                            .permissions(new HashSet<>(permissions)) // 👈 Garantie d'une collection modifiable
                            .build());
        }
    }
}
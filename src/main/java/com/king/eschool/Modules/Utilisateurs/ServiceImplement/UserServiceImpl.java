package com.king.eschool.Modules.Utilisateurs.ServiceImplement;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Audite.ServiceImpl.AuditService;
import com.king.eschool.Core.config.EmailServiceImpl;
import com.king.eschool.Core.dtoRequest.LoginRequest;
import com.king.eschool.Core.dtoResponse.AuthResponse;
import com.king.eschool.Core.dtoResponse.SchoolInfo;
import com.king.eschool.Core.dtoResponse.UserAuthInfo;
import com.king.eschool.Core.jwt.JwtService;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.Modules.Utilisateurs.Dto.request.CompleteActivationDto;
import com.king.eschool.Modules.Utilisateurs.Dto.request.CreateUserDto;
import com.king.eschool.Modules.Utilisateurs.Models.Permission;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.Repository.RoleRepository;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final SchoolRepository schoolRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final AuditService auditService;
        private final EmailServiceImpl emailServiceImpl;

        public UserServiceImpl(
                        UserRepository userRepository,
                        RoleRepository roleRepository,
                        SchoolRepository schoolRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        EmailServiceImpl emailServiceImpl,
                        AuthenticationManager authenticationManager,
                        AuditService auditService) {
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.schoolRepository = schoolRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
                this.authenticationManager = authenticationManager;
                this.auditService = auditService;
                this.emailServiceImpl = emailServiceImpl;
        }

        @Transactional
        public User createUser(CreateUserDto dto) {
                if (userRepository.existsByEmail(dto.getEmail())) {
                        throw new IllegalArgumentException("L'adresse email est déjà utilisée.");
                }

                // 1. Recherche des rôles par UUID (findById)
                Set<Role> assignedRoles = dto.getRoleIds().stream()
                                .map(id -> roleRepository.findById(id)
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Rôle introuvable avec l'ID : " + id)))
                                .collect(Collectors.toSet());

                // 2. Vérification sécurisée du rôle SUPER_ADMIN (gestion des valeurs nulles sur
                // slug/code)
                boolean isSuperAdmin = assignedRoles.stream()
                                .anyMatch(role -> role.getSlug() != null &&
                                                (role.getSlug().equalsIgnoreCase("ROLE_SUPER_ADMIN") ||
                                                                role.getSlug().equalsIgnoreCase("SUPER_ADMIN")));

                // 3. Validation de l'établissement rattaché
                UUID targetSchoolId = null;
                if (!isSuperAdmin) {
                        if (dto.getSchoolId() == null) {
                                throw new IllegalArgumentException(
                                                "Un établissement (schoolId) est obligatoire pour ce type d'utilisateur.");
                        }
                        School school = schoolRepository.findById(dto.getSchoolId())
                                        .orElseThrow(() -> new RuntimeException("Établissement introuvable."));
                        targetSchoolId = school.getId();
                }

                // 4. Génération du token unique d'activation
                String activationToken = UUID.randomUUID().toString();

                // 5. Construction du nouvel utilisateur
                User user = User.builder()
                                .schoolId(targetSchoolId)
                                .campusId(dto.getCampusId())
                                .username(dto.getEmail())
                                .firstName(dto.getFirstName())
                                .lastName(dto.getLastName())
                                .email(dto.getEmail())
                                .phone(dto.getPhone())
                                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString())) // Mot de passe
                                                                                                    // temporaire
                                .status(User.UserStatus.PENDING_ACTIVATION)
                                .activationToken(activationToken)
                                .activationTokenExpiry(LocalDateTime.now().plusHours(48))
                                .roles(assignedRoles)
                                .build();

                User savedUser = userRepository.save(user);

                // 6. Envoi de l'e-mail d'activation
                emailServiceImpl.sendActivationEmail(savedUser.getEmail(), savedUser.getFirstName(), activationToken);

                // 7. Journal d'audit
                auditService.logAction(
                                savedUser.getId(),
                                savedUser.getEmail(),
                                targetSchoolId,
                                "USER_CREATED",
                                "Création du compte et envoi de l'e-mail d'activation.",
                                "SYSTEM");

                return savedUser;
        }

        @Transactional
        public void activateAccount(
                        String token,
                        String password) {

                User user = userRepository
                                .findByActivationToken(token)
                                .orElseThrow(
                                                () -> new RuntimeException(
                                                                "Lien d'activation invalide ou expiré."));

                if (user.getActivationTokenExpiry() == null
                                ||
                                user.getActivationTokenExpiry()
                                                .isBefore(LocalDateTime.now())) {

                        throw new RuntimeException(
                                        "Lien d'activation invalide ou expiré.");
                }

                user.setPasswordHash(
                                new BCryptPasswordEncoder()
                                                .encode(password));

                user.setActivationToken(null);

                user.setActivationTokenExpiry(null);

                user.setStatus(
                                User.UserStatus.ACTIVE);

                user.setActive(true);

                user.setFailedLoginAttempts(0);

                userRepository.save(user);
        }

        @Transactional
        public User updateUser(UUID userId, CreateUserDto dto) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

                // Mise à jour des informations de base
                if (dto.getFirstName() != null)
                        user.setFirstName(dto.getFirstName());
                if (dto.getLastName() != null)
                        user.setLastName(dto.getLastName());
                if (dto.getPhone() != null)
                        user.setPhone(dto.getPhone());
                if (dto.getCampusId() != null)
                        user.setCampusId(dto.getCampusId());

                User updatedUser = userRepository.save(user);

                auditService.logAction(
                                updatedUser.getId(),
                                updatedUser.getEmail(),
                                updatedUser.getSchoolId(),
                                "USER_UPDATED",
                                "Mise à jour des informations du profil utilisateur.",
                                "ADMIN");

                return updatedUser;
        }

        @Transactional
        public void assignRolesToUser(UUID userId, List<String> roleSlugs, UUID currentAdminSchoolId,
                        boolean isSuperAdmin) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

                // Contrôle de granularité / multi-tenant (RG-USR-004)
                if (!isSuperAdmin && !user.getSchoolId().equals(currentAdminSchoolId)) {
                        throw new SecurityException(
                                        "Accès refusé : vous ne pouvez modifier que les utilisateurs de votre propre établissement.");
                }

                Set<Role> newRoles = roleSlugs.stream()
                                .map(slug -> roleRepository.findBySlug(slug)
                                                .orElseThrow(() -> new RuntimeException("Rôle introuvable : " + slug)))
                                .collect(Collectors.toSet());

                user.setRoles(newRoles);
                userRepository.save(user);

                auditService.logAction(
                                user.getId(),
                                user.getEmail(),
                                user.getSchoolId(),
                                "ROLES_ASSIGNED",
                                "Modification des rôles attribués à l'utilisateur : " + roleSlugs,
                                "ADMIN");
        }

        @Transactional
        public AuthResponse login(LoginRequest request) {

                User user = userRepository
                                .findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException(
                                                "Identifiants incorrects."));

                if (user.getStatus() == User.UserStatus.LOCKED ||
                                user.getStatus() == User.UserStatus.SUSPENDED) {

                        throw new RuntimeException(
                                        "Ce compte est verrouillé ou suspendu.");
                }

                try {

                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));

                } catch (Exception e) {

                        user.setFailedLoginAttempts(
                                        user.getFailedLoginAttempts() + 1);

                        if (user.getFailedLoginAttempts() >= 5) {

                                user.setStatus(
                                                User.UserStatus.LOCKED);

                                auditService.logAction(
                                                user.getId(),
                                                user.getEmail(),
                                                user.getSchoolId(),
                                                "ACCOUNT_LOCKED",
                                                "Verrouillage après plusieurs échecs.",
                                                "CLIENT");
                        }

                        userRepository.save(user);

                        auditService.logAction(
                                        user.getId(),
                                        user.getEmail(),
                                        user.getSchoolId(),
                                        "LOGIN_FAILED",
                                        "Tentative de connexion échouée.",
                                        "CLIENT");

                        throw new RuntimeException(
                                        "Identifiants incorrects.");
                }

                if (user.getStatus() == User.UserStatus.PENDING_ACTIVATION) {

                        throw new RuntimeException(
                                        "Veuillez activer votre compte.");
                }

                user.setFailedLoginAttempts(0);

                userRepository.save(user);

                String accessToken = jwtService.generateToken(user);

                SchoolInfo schoolInfo = buildSchoolInfo(user);

                List<String> roles = user.getRoles()
                                .stream()
                                .map(Role::getSlug)
                                .toList();

                List<String> permissions = user.getRoles()
                                .stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(Permission::getCode)
                                .distinct()
                                .toList();

                auditService.logAction(
                                user.getId(),
                                user.getEmail(),
                                user.getSchoolId(),
                                "LOGIN_SUCCESS",
                                "Connexion réussie.",
                                "CLIENT");

                return AuthResponse.builder()

                                .token(accessToken)

                                // à implémenter lorsque le refresh token
                                // sera ajouté
                                .refreshToken(null)

                                .user(
                                                UserAuthInfo.builder()
                                                                .id(user.getId())
                                                                .email(user.getEmail())
                                                                .firstName(user.getFirstName())
                                                                .lastName(user.getLastName())
                                                                .phone(user.getPhone())
                                                                .status(
                                                                                user.getStatus().name())
                                                                .roles(roles)
                                                                .build())

                                .school(schoolInfo)

                                .permissions(permissions)

                                .build();
        }

        private SchoolInfo buildSchoolInfo(User user) {

                if (user.getSchoolId() == null) {
                        return null;
                }

                School school = schoolRepository
                                .findById(user.getSchoolId())
                                .orElse(null);

                if (school == null) {
                        return null;
                }

                return SchoolInfo.builder()
                                .id(school.getId())
                                .name(school.getName())
                                .code(school.getCode())
                                .email(school.getEmail())
                                .phone(school.getPhone())
                                .logoUrl(school.getLogoUrl())
                                .currency(school.getCurrency())
                                .timezone(school.getTimezone())
                                .domain(school.getDomain())
                                .status(school.getStatus().name())
                                .build();
        }

        @Transactional
        public void softDeleteUser(UUID id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
                user.setDeletedAt(LocalDateTime.now());
                user.setStatus(User.UserStatus.SUSPENDED);
                userRepository.save(user);

                auditService.logAction(user.getId(), user.getEmail(), user.getSchoolId(), "USER_DELETED",
                                "Suppression logique (soft-delete) du compte.", "ADMIN");
        }

        public List<User> getAllUsers() {
                return userRepository.findAll().stream()
                                .filter(u -> u.getDeletedAt() == null)
                                .collect(Collectors.toList());
        }

       /**
     * Recherche un utilisateur par son token d'activation
     */
    public Optional<User> findByActivationToken(String token) {
        return userRepository.findByActivationToken(token);
    }

    /**
     * Finalise l'activation du compte utilisateur
     */
    @Transactional
    public void completeUserActivation(CompleteActivationDto dto) {
        // 1. Récupération et vérification de l'utilisateur via le token
        User user = userRepository.findByActivationToken(dto.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token d'activation invalide ou introuvable."));

        // 2. Vérification de l'expiration du token
        if (user.getActivationTokenExpiry() != null && user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Le lien d'activation a expiré. Veuillez demander un nouveau lien.");
        }

        // 3. Mise à jour du mot de passe (hachage)
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        // 4. Changement du statut du compte et invalidation du token d'activation
        user.setStatus(User.UserStatus.ACTIVE);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);

        // 5. Mise à jour du téléphone si renseigné
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user.setPhone(dto.getPhone());
        }

        // 6. Sauvegarde de l'utilisateur mis à jour
        User savedUser = userRepository.save(user);

        // 🟢 Envoi de l'e-mail de confirmation d'activation
    emailServiceImpl.sendActivationSuccessEmail(savedUser.getEmail(), savedUser.getFirstName());

        // 7. Le journal d'audit enregistre l'action d'activation
        auditService.logAction(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getSchoolId(),
                "USER_ACTIVATED",
                "Le compte utilisateur a été activé avec succès.",
                "USER"
        );
}

}
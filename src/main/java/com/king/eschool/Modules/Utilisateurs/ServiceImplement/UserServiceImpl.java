package com.king.eschool.Modules.Utilisateurs.ServiceImplement;

import com.king.eschool.Audite.ServiceImpl.AuditService;
import com.king.eschool.Audite.models.AuditEvent;
import com.king.eschool.Core.config.EmailServiceImpl;
import com.king.eschool.Core.dtoRequest.LoginRequest;
import com.king.eschool.Core.dtoResponse.AuthResponse;
import com.king.eschool.Core.dtoResponse.SchoolInfo;
import com.king.eschool.Core.jwt.JwtService;
import com.king.eschool.Modules.School.Models.Campus;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.CampusRepository;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.Modules.Utilisateurs.Dto.reponse.UserMapper;
import com.king.eschool.Modules.Utilisateurs.Dto.reponse.UserResponseDto;
import com.king.eschool.Modules.Utilisateurs.Dto.request.AssignAccessDto;
import com.king.eschool.Modules.Utilisateurs.Dto.request.CompleteActivationDto;
import com.king.eschool.Modules.Utilisateurs.Dto.request.CreateUserDto;
import com.king.eschool.Modules.Utilisateurs.Models.Permission;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.Repository.PermissionRepository;
import com.king.eschool.Modules.Utilisateurs.Repository.RoleRepository;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
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
    private final CampusRepository campusRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;
    private final EmailServiceImpl emailServiceImpl;
    private final UserMapper userMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            SchoolRepository schoolRepository,
            CampusRepository campusRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailServiceImpl emailServiceImpl,
            AuthenticationManager authenticationManager,
            AuditService auditService,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailServiceImpl = emailServiceImpl;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
        this.userMapper = userMapper;
    }

    @Transactional
    public User createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("L'adresse email est déjà utilisée.");
        }

        // 1.// Recherche des rôles uniquement par UUID (findById)
                Set<Role> assignedRoles = dto.getRoleIds().stream()
        .map(roleId -> roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle introuvable avec l'ID : " + roleId)))
        .collect(Collectors.toSet());

        // 2. Vérification du rôle SUPER_ADMIN
        boolean isSuperAdmin = assignedRoles.stream()
                .anyMatch(role -> role.getSlug() != null &&
                        (role.getSlug().equalsIgnoreCase("ROLE_SUPER_ADMIN") ||
                         role.getSlug().equalsIgnoreCase("SUPER_ADMIN")));

        // 3. Validation de l'établissement rattaché
        UUID targetSchoolId = null;
        if (!isSuperAdmin) {
            if (dto.getSchoolId() == null) {
                throw new IllegalArgumentException("Un établissement (schoolId) est obligatoire pour ce type d'utilisateur.");
            }
            School school = schoolRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Établissement introuvable."));
            targetSchoolId = school.getId();
        }

        // 4. Génération du token d'activation
        String activationToken = UUID.randomUUID().toString();

        // 5. Construction de l'utilisateur
        User user = User.builder()
                .schoolId(targetSchoolId)
                .campusId(dto.getCampusId())
                .username(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .status(User.UserStatus.PENDING_ACTIVATION)
                .activationToken(activationToken)
                .activationTokenExpiry(LocalDateTime.now().plusHours(48))
                .roles(assignedRoles)
                .build();

        User savedUser = userRepository.save(user);

        // 6. Envoi de l'e-mail d'activation
        emailServiceImpl.sendActivationEmail(savedUser.getEmail(), savedUser.getFirstName(), activationToken);

        // 7. Journal d'audit
        auditService.logEvent(AuditEvent.builder()
                .userId(savedUser.getId())
                .username(savedUser.getEmail())
                .schoolId(targetSchoolId)
                .actionType("USER_CREATED")
                .targetEntity("USER")
                .targetId(savedUser.getId().toString())
                .details("Création du compte et envoi de l'e-mail d'activation.")
                .build());

        return savedUser;
    }

    @Transactional
    public void activateAccount(String token, String password) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Lien d'activation invalide ou expiré."));

        if (user.getActivationTokenExpiry() == null || user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Lien d'activation invalide ou expiré.");
        }

        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setActive(true);
        user.setFailedLoginAttempts(0);

        userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID userId, CreateUserDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getCampusId() != null) user.setCampusId(dto.getCampusId());

        User updatedUser = userRepository.save(user);

        auditService.logEvent(AuditEvent.builder()
                .userId(updatedUser.getId())
                .username(updatedUser.getEmail())
                .schoolId(updatedUser.getSchoolId())
                .actionType("USER_UPDATED")
                .targetEntity("USER")
                .targetId(updatedUser.getId().toString())
                .details("Mise à jour des informations du profil utilisateur.")
                .build());

        return updatedUser;
    }

    @Transactional
    public void assignRolesToUser(UUID userId, List<String> roleSlugs, UUID currentAdminSchoolId, boolean isSuperAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

        List<String> oldRoles = user.getRoles().stream().map(Role::getSlug).toList();

        Set<Role> newRoles = roleSlugs.stream()
                .map(slug -> roleRepository.findBySlug(slug)
                        .orElseThrow(() -> new RuntimeException("Rôle introuvable : " + slug)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);
        userRepository.save(user);

        auditService.logEvent(AuditEvent.builder()
                .schoolId(user.getSchoolId())
                .actionType("UPDATE_ROLES")
                .targetEntity("USER")
                .targetId(user.getId().toString())
                .oldValue(oldRoles)
                .newValue(roleSlugs)
                .details("Modification des rôles applicatifs.")
                .build());
    }

@Transactional
public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
    
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Identifiants incorrects."));

    String ip = httpRequest.getRemoteAddr();
    String agent = httpRequest.getHeader("User-Agent");

    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (Exception e) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= 5) {
            user.setStatus(User.UserStatus.LOCKED);
            auditService.logEvent(AuditEvent.builder()
                    .userId(user.getId())
                    .username(user.getEmail())
                    .schoolId(user.getSchoolId())
                    .ipAddress(ip)
                    .deviceInfo(agent)
                    .actionType("ACCOUNT_LOCKED")
                    .targetEntity("USER")
                    .targetId(user.getId().toString())
                    .details("Verrouillage automatique après 5 échecs.")
                    .build());
        }
        userRepository.save(user);

        auditService.logEvent(AuditEvent.builder()
                .username(request.getEmail())
                .ipAddress(ip)
                .deviceInfo(agent)
                .actionType("LOGIN_FAILED")
                .targetEntity("AUTHENTICATION")
                .details("Tentative de connexion échouée.")
                .build());

        // ❌ LOG CONSOLE EN CAS D'ÉCHEC
        System.err.println("❌ ECHEC DE CONNEXION pour l'utilisateur : " + request.getEmail());

        throw new RuntimeException("Identifiants incorrects.");
    }

    user.setFailedLoginAttempts(0);
    userRepository.save(user);

    // 1. Tokens
    String accessToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    // 2. Récupération de l'École et du Campus
    School school = user.getSchoolId() != null 
            ? schoolRepository.findById(user.getSchoolId()).orElse(null) 
            : null;

    Campus campus = user.getCampusId() != null 
            ? campusRepository.findById(user.getCampusId()).orElse(null) 
            : null;

    // 3. Rôles et Permissions
    List<String> roleSlugs = user.getRoles() != null
            ? user.getRoles().stream().map(Role::getSlug).collect(Collectors.toList())
            : Collections.emptyList();

    String primaryRole = roleSlugs.isEmpty() ? null : roleSlugs.get(0);

    Set<String> permissionSlugs = user.getRoles() != null
            ? user.getRoles().stream()
                    .filter(role -> role.getPermissions() != null)
                    .flatMap(role -> role.getPermissions().stream())
                    .map(Permission::getSlug)
                    .collect(Collectors.toSet())
            : Collections.emptySet();

    // 4. Construction du DTO utilisateur enrichi
    UserResponseDto userDto = userMapper.toResponseDto(user, school, campus);

    // 5. Traçabilité
    auditService.logEvent(AuditEvent.builder()
            .userId(user.getId())
            .username(user.getEmail())
            .userRole(String.join(",", roleSlugs))
            .schoolId(user.getSchoolId())
            .campusId(user.getCampusId())
            .ipAddress(ip)
            .deviceInfo(agent)
            .actionType("LOGIN_SUCCESS")
            .targetEntity("AUTHENTICATION")
            .build());

    // 🟢 LOGS CONSOLE EN CAS DE SUCCÈS
    System.out.println("==================================================");
    System.out.println("🟢 CONNEXION RÉUSSIE !");
    System.out.println("--------------------------------------------------");
    System.out.println("👤 Utilisateur  : " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
    System.out.println("🔑 Rôle Principal: " + primaryRole);
    System.out.println("🎭 Rôles        : " + roleSlugs);
    System.out.println("🏫 École        : " + (school != null ? school.getName() + " (ID: " + school.getId() + ")" : "Aucune"));
    System.out.println("🏢 Campus       : " + (campus != null ? campus.getName() + " (ID: " + campus.getId() + ")" : "Aucun"));
    System.out.println("🔐 Permissions   : " + permissionSlugs);
    System.out.println("🎟️ Access Token : " + accessToken.substring(0, Math.min(accessToken.length(), 20)) + "...");
    System.out.println("==================================================");

    // 6. Réponse HTTP
    return AuthResponse.builder()
            .token(accessToken)
            .refreshToken(refreshToken)
            .user(userDto)
            .school(userMapper.toSchoolInfo(school))
            .campus(userMapper.toCampusInfo(campus))
            .role(primaryRole)
            .roles(roleSlugs)
            .permissions(permissionSlugs)
            .build();
}

    private SchoolInfo buildSchoolInfo(User user) {
        if (user.getSchoolId() == null) {
            return null;
        }

        School school = schoolRepository.findById(user.getSchoolId()).orElse(null);
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

        auditService.logEvent(AuditEvent.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .schoolId(user.getSchoolId())
                .actionType("USER_DELETED")
                .targetEntity("USER")
                .targetId(user.getId().toString())
                .details("Suppression logique (soft-delete) du compte.")
                .build());
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::mapToUserResponseDto)
                .collect(Collectors.toList());
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        Set<UserResponseDto.PermissionResponseDto> allPermissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(perm -> UserResponseDto.PermissionResponseDto.builder()
                        .id(perm.getId())
                        .name(perm.getName())
                        .slug(perm.getSlug())
                        .category(perm.getCode())
                        .build())
                .collect(Collectors.toSet());

        Set<UserResponseDto.RoleResponseDto> rolesDto = user.getRoles().stream()
                .map(role -> UserResponseDto.RoleResponseDto.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .slug(role.getSlug())
                        .system(role.isSystem())
                        .permissions(role.getPermissions().stream()
                                .map(perm -> UserResponseDto.PermissionResponseDto.builder()
                                        .id(perm.getId())
                                        .name(perm.getName())
                                        .slug(perm.getSlug())
                                        .category(perm.getCode())
                                        .build())
                                .collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toSet());

        UserResponseDto.SchoolInfo schoolInfo = null;
        String schoolName = null;

        if (user.getSchoolId() != null) {
            Optional<School> optionalSchool = schoolRepository.findById(user.getSchoolId());
            if (optionalSchool.isPresent()) {
                School school = optionalSchool.get();
                schoolName = school.getName();
                schoolInfo = UserResponseDto.SchoolInfo.builder()
                        .id(school.getId())
                        .name(school.getName())
                        .code(school.getCode())
                        .email(school.getEmail())
                        .phone(school.getPhone())
                        .logoUrl(school.getLogoUrl())
                        .currency(school.getCurrency())
                        .timezone(school.getTimezone())
                        .domain(school.getDomain())
                        .status(school.getStatus() != null ? school.getStatus().name() : "ACTIVE")
                        .build();
            }
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .schoolId(user.getSchoolId())
                .campusId(user.getCampusId())
                .schoolName(schoolName)
                .school(schoolInfo)
                .roles(rolesDto)
                .permissions(allPermissions)
                .build();
    }

    public Optional<User> findByActivationToken(String token) {
        return userRepository.findByActivationToken(token);
    }

    @Transactional
    public void completeUserActivation(CompleteActivationDto dto) {
        User user = userRepository.findByActivationToken(dto.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token d'activation invalide ou introuvable."));

        if (user.getActivationTokenExpiry() != null && user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Le lien d'activation a expiré. Veuillez demander un nouveau lien.");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user.setPhone(dto.getPhone());
        }

        User savedUser = userRepository.save(user);

        emailServiceImpl.sendActivationSuccessEmail(savedUser.getEmail(), savedUser.getFirstName());

        auditService.logEvent(AuditEvent.builder()
                .userId(savedUser.getId())
                .username(savedUser.getEmail())
                .schoolId(savedUser.getSchoolId())
                .actionType("USER_ACTIVATED")
                .targetEntity("USER")
                .targetId(savedUser.getId().toString())
                .details("Le compte utilisateur a été activé avec succès.")
                .build());
    }

    @Transactional
    public User updateUserAccess(UUID userId, AssignAccessDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + userId));

        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            user.setRoles(roles);
        }

        if (dto.getPermissionIds() != null && !user.getRoles().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(dto.getPermissionIds()));
            Role userRole = user.getRoles().iterator().next();
            userRole.setPermissions(permissions);
            roleRepository.save(userRole);
        }

        return userRepository.save(user);
    }
}
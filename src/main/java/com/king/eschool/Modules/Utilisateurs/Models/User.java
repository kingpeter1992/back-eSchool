package com.king.eschool.Modules.Utilisateurs.Models;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "school_id")
    private UUID schoolId; // Null pour les Super Admins SaaS

    @Column(name = "campus_id")
    private UUID campusId;

    @Column(name = "activation_token")
    private String activationToken; // Jeton unique pour définir le premier mot de passe

    private LocalDateTime activationTokenExpiry; // Date d'expiration du jeton d'activation

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

     @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 30)
    private String phone;

    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_ACTIVATION;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private int failedLoginAttempts = 0;

     // ============================
    // RESET PASSWORD
    // ============================

    @Column(name = "reset_token", unique = true)
    private String resetToken;


    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public enum UserStatus {
        PENDING_ACTIVATION, ACTIVE, SUSPENDED, LOCKED
    }

}

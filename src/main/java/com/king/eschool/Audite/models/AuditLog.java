package com.king.eschool.Audite.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder 
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // 👤 Qui ?
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 150)
    private String username;

    // 🟢 Augmentation de la taille pour éviter les conflits quand l'utilisateur a plusieurs rôles
    @Column(name = "user_role", length = 500)
    private String userRole;

    // 📅 Quand ?
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    // 🏫 Dans quelle école ? & 🏢 Campus ?
    @Column(name = "school_id")
    private UUID schoolId;

    @Column(name = "campus_id")
    private UUID campusId;

    // 🌐 IP & 💻 Appareil
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // 🎯 Action & 📦 Ressource
    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType; // CREATE, UPDATE, DELETE, LOGIN, EXPORT...

    @Column(name = "target_entity", nullable = false, length = 100)
    private String targetEntity; // SCHOOL, CAMPUS, USER...

    @Column(name = "target_id", length = 255)
    private String targetId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "device_info", columnDefinition = "TEXT")
    private String deviceInfo;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
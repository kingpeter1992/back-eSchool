package com.king.eschool.Audite.models;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
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

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(name = "school_id")
    private UUID schoolId;

    @Column(nullable = false, length = 100)
    private String action; // Ex: USER_CREATED, LOGIN_FAILED, ACCESS_DENIED

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
package com.king.eschool.Modules.School.Models;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(unique = true, length = 150)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = SchoolStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum SchoolStatus {
        ACTIVE, PENDING, SUSPENDED, DELETED
    }
}
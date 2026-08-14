package com.king.eschool.Modules.School.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "campuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    @Builder.Default
    private String country = "RDC";

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CampusStatus status = CampusStatus.ACTIVE;

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
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum CampusStatus {
        ACTIVE, INACTIVE
    }
}

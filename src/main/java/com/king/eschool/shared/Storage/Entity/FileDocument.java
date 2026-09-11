package com.king.eschool.shared.Storage.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDocument {

    @Id
    private UUID id;

    private String originalName;

    private String fileName;

    private String contentType;

    private Long size;

    @Column(length = 1000)
    private String storagePath;

    @Column(length = 1500)
    private String publicUrl;

    private String module;

    private Long referenceId;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

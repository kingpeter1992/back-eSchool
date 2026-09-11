package com.king.eschool.shared.Storage.dtoResponse;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FileDocumentResponse {
    private UUID id;
    private String originalName;
    private String fileName;
    private String contentType;
    private Long size;
    private String storagePath;
    private String publicUrl;
    private String module;
    private Long referenceId;
    private LocalDateTime createdAt;
}

package com.king.eschool.shared.Storage.dtoResponse;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileDocumentResponse {
    private Long id;
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

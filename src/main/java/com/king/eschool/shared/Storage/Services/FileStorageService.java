package com.king.eschool.shared.Storage.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.shared.Storage.Entity.FileDocument;
import com.king.eschool.shared.Storage.dao.FileDocumentRepository;
import com.king.eschool.shared.Storage.dtoResponse.FileDocumentResponse;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileStorageService {
    private final FileDocumentRepository repository;
    private final RestTemplate restTemplate;
    public FileStorageService(FileDocumentRepository repository, 
        RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
}

    @Value("${supabase.storage-url}")
    private String storageUrl;

    @Value("${supabase.bucket}")
    private String bucket;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.url}")
    private String supabaseUrl;

    public FileDocumentResponse uploadFile(
            MultipartFile file,
            String module,
            Long referenceId
    ) {

        try {

            validateFile(file);

            String cleanName =
                    cleanFileName(file.getOriginalFilename());

            String fileName =
                    UUID.randomUUID() + "-" + cleanName;

            String storagePath =
                    module + "/"
                            + referenceId + "/"
                            + fileName;

            String uploadUrl =
                    storageUrl
                            + "/object/"
                            + bucket
                            + "/"
                            + storagePath;

            String key = serviceRoleKey.trim();

            HttpHeaders headers = new HttpHeaders();

            headers.setBearerAuth(key);

            headers.set("apikey", key);

            headers.setContentType(
                    MediaType.parseMediaType(
                            file.getContentType() != null
                                    ? file.getContentType()
                                    : "application/octet-stream"
                    )
            );

            headers.set("x-upsert", "true");

            HttpEntity<byte[]> request =
                    new HttpEntity<>(
                            file.getBytes(),
                            headers
                    );

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            uploadUrl,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

            if (!response.getStatusCode().is2xxSuccessful()) {

                throw new RuntimeException(
                        "Erreur upload Supabase : "
                                + response.getBody()
                );
            }

            String publicUrl =
                    supabaseUrl
                            + "/storage/v1/object/public/"
                            + bucket
                            + "/"
                            + storagePath;

            FileDocument saved =
                    repository.save(
                            FileDocument.builder()
                                    .originalName(file.getOriginalFilename())
                                    .fileName(fileName)
                                    .contentType(file.getContentType())
                                    .size(file.getSize())
                                    .storagePath(storagePath)
                                    .publicUrl(publicUrl)
                                    .module(module)
                                    .referenceId(referenceId)
                                    .build()
                    );

            return toResponse(saved);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Impossible d’uploader le fichier : "
                            + e.getMessage(),
                    e
            );
        }
    }

    public List<FileDocumentResponse> getFiles(
            String module,
            Long referenceId
    ) {

        return repository
                .findByModuleAndReferenceId(
                        module,
                        referenceId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new RuntimeException(
                    "Le fichier dépasse 50MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new RuntimeException(
                    "Type de fichier inconnu"
            );
        }

        List<String> allowedTypes = List.of(
                "application/pdf",
                "image/png",
                "image/jpeg",
                "image/jpg",
                "video/mp4",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        if (!allowedTypes.contains(contentType)) {

            throw new RuntimeException(
                    "Type de fichier non autorisé : "
                            + contentType
            );
        }
    }

    private String cleanFileName(String originalName) {

        if (originalName == null) {
            return "file";
        }

        String normalized =
                Normalizer.normalize(
                                originalName,
                                Normalizer.Form.NFD
                        )
                        .replaceAll("\\p{M}", "");

        return normalized
                .replaceAll("[^a-zA-Z0-9\\.\\-_]", "-")
                .replaceAll("-+", "-")
                .toLowerCase();
    }

    private FileDocumentResponse toResponse(
            FileDocument file
    ) {

        return FileDocumentResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .fileName(file.getFileName())
                .contentType(file.getContentType())
                .size(file.getSize())
                .storagePath(file.getStoragePath())
                .publicUrl(file.getPublicUrl())
                .module(file.getModule())
                .referenceId(file.getReferenceId())
                .createdAt(file.getCreatedAt())
                .build();
    }


    public void deleteFileByUrl(String publicUrl) {

    try {

        FileDocument file = repository
                .findByPublicUrl(publicUrl)
                .orElse(null);

        if (file == null) {
            return;
        }

        String deleteUrl =
                storageUrl
                        + "/object/"
                        + bucket
                        + "/"
                        + file.getStoragePath();

        String key = serviceRoleKey.trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(key);
        headers.set("apikey", key);

        HttpEntity<Void> entity =
                new HttpEntity<>(headers);

        restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                entity,
                String.class
        );

        repository.delete(file);

    } catch (Exception e) {

        throw new RuntimeException(
                "Erreur suppression fichier : "
                        + e.getMessage(),
                e
        );
    }
}
}
package com.king.eschool.shared.Storage.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.shared.Storage.Entity.FileDocument;

import java.util.List;
import java.util.Optional;

public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {

    List<FileDocument> findByModuleAndReferenceId(String module, Long referenceId);

    Optional<FileDocument> findByPublicUrl(
            String publicUrl
    );
}

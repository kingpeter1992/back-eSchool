package com.king.eschool.Audite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Audite.models.AuditLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findBySchoolId(UUID schoolId);
    List<AuditLog> findByUserId(UUID userId);
}
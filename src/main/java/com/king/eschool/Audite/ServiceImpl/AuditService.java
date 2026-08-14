package com.king.eschool.Audite.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Audite.models.AuditLog;
import com.king.eschool.Audite.repository.AuditLogRepository;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(UUID userId, String userEmail, UUID schoolId, String action, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .schoolId(schoolId)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }
}
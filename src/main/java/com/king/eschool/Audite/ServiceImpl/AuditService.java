package com.king.eschool.Audite.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.king.eschool.Audite.models.AuditEvent;
import com.king.eschool.Audite.models.AuditLog;
import com.king.eschool.Audite.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Service
@RequiredArgsConstructor 
public class AuditService {

   private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    private String safeTruncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(AuditEvent event) {
        AuditLog log = AuditLog.builder()
                .userId(event.getUserId())
                .username(safeTruncate(event.getUsername() != null ? event.getUsername() : "ANONYMOUS", 150))
                .userRole(safeTruncate(event.getUserRole() != null ? event.getUserRole() : "NONE", 500))
                .timestamp(Instant.now())
                .schoolId(event.getSchoolId())
                .campusId(event.getCampusId())
                .ipAddress(safeTruncate(event.getIpAddress(), 45))
                .actionType(safeTruncate(event.getActionType(), 100))
                .targetEntity(safeTruncate(event.getTargetEntity(), 100))
                .targetId(safeTruncate(event.getTargetId(), 255))
                .oldValue(toJson(event.getOldValue()))
                .newValue(toJson(event.getNewValue()))
                .deviceInfo(event.getDeviceInfo()) // Déjà en TEXT dans l'entité
                .details(safeTruncate(event.getDetails(), 2000))
                .build();

        auditLogRepository.save(log);
    }

    private String toJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return object.toString();
        }
    }
}
package com.king.eschool.Audite;


import com.king.eschool.Audite.ServiceImpl.AuditLogSpecifications;
import com.king.eschool.Audite.ServiceImpl.AuditService;
import com.king.eschool.Audite.models.AuditLog;
import com.king.eschool.Audite.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs") // 👈 Vérifiez exactement cette route
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) UUID schoolId,
            @RequestParam(required = false) UUID campusId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            Pageable pageable) {

        Specification<AuditLog> spec = AuditLogSpecifications.filterLogs(
                schoolId, campusId, actionType, fromDate, toDate
        );

        Page<AuditLog> logs = auditLogRepository.findAll(spec, pageable);
        return ResponseEntity.ok(logs);
    }
}
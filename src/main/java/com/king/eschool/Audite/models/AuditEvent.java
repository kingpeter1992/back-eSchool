package com.king.eschool.Audite.models;

import java.util.UUID;

import lombok.*;

@Data
@Builder 
@AllArgsConstructor
@NoArgsConstructor
public class AuditEvent {
    private UUID userId;
    private String username;
    private String userRole;
    private UUID schoolId;
    private UUID campusId;
    private String ipAddress;
    private String deviceInfo;
    private String actionType;
    private String targetEntity;
    private String targetId;
    private Object oldValue;
    private Object newValue;
    private String details;
}

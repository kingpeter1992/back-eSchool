package com.king.eschool.Audite;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.king.eschool.Audite.ServiceImpl.AuditService;
import com.king.eschool.Audite.models.AuditEvent;
import com.king.eschool.Core.config.UserPrincipal;
import java.lang.reflect.Method;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor 
public class AuditAspect {

    private final AuditService auditService;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAuditSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        AuditEvent event = buildBaseEvent(auditable);
        
        // Tentative d'extraction de l'ID cible à partir du DTO ou entité retournée
        if (result != null) {
            event.setTargetId(extractEntityId(result));
            event.setNewValue(result);
        }

        auditService.logEvent(event);
    }

    private AuditEvent buildBaseEvent(Auditable auditable) {
        AuditEvent event = AuditEvent.builder()
                .actionType(auditable.action())
                .targetEntity(auditable.targetEntity())
                .build();

        // 1. Context Utilisateur (Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            event.setUsername(auth.getName());
            event.setUserRole(auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(",")));

            if (auth.getPrincipal() instanceof UserPrincipal principal) {
                event.setUserId(principal.getId());
                event.setSchoolId(principal.getSchoolId());
                event.setCampusId(principal.getCampusId());
            }
        }

        // 2. Context HTTP (IP & Device)
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            event.setIpAddress((ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip.split(",")[0]);
            event.setDeviceInfo(request.getHeader("User-Agent"));
        }

        return event;
    }

    private String extractEntityId(Object obj) {
        try {
            Method getIdMethod = obj.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(obj);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
package com.king.eschool.Audite;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.king.eschool.Audite.ServiceImpl.AuditService;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;
    private final UserRepository userRepository;

    public AuditAspect(AuditService auditService, UserRepository userRepository) {
        this.auditService = auditService;
        this.userRepository = userRepository;
    }

    @AfterReturning("@annotation(auditable)")
    public void logAudit(JoinPoint joinPoint, Auditable auditable) {
        // 1. Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = "ANONYMOUS";
        UUID currentUserId = null;
        UUID currentSchoolId = null;

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            currentUserEmail = authentication.getName();
            User user = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (user != null) {
                currentUserId = user.getId();
                currentSchoolId = user.getSchoolId();
            }
        }

        // 2. Récupérer l'adresse IP du client
        String clientIp = "UNKNOWN";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = request.getRemoteAddr();
            }
        }

        // 3. Extraire les détails de la méthode exécutée
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String details = "Exécution de la méthode : " + method.getDeclaringClass().getSimpleName() + "." + method.getName();

        if (!auditable.targetEntity().isEmpty()) {
            details += " | Cible : " + auditable.targetEntity();
        }

        // 4. Sauvegarde persistante via le service d'audit
        auditService.logAction(
                currentUserId,
                currentUserEmail,
                currentSchoolId,
                auditable.action(),
                details,
                clientIp
        );
    }
}
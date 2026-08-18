package com.king.eschool.shared.CurrentUser;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@Component
public class SecurityUtils {

    /**
     * Récupère l'ID de l'école (school_id) de l'utilisateur actuellement connecté.
     * Renvoie null s'il s'agit d'un Super Admin ou si la clé n'existe pas.
     */
    public UUID getCurrentUserSchoolId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String schoolIdStr = jwt.getClaimAsString("school_id");
            if (schoolIdStr != null && !schoolIdStr.isBlank()) {
                return UUID.fromString(schoolIdStr);
            }
        }
        return null;
    }

    /**
     * Vérifie si l'utilisateur actuellement connecté est un Super Administrateur.
     */
    public boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"));
        }
        return false;
    }

    /**
     * Récupère l'ID (UUID) de l'utilisateur connecté depuis le JWT (claim "sub" ou "user_id").
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String userIdStr = jwt.getClaimAsString("user_id"); // Ou jwt.getSubject()
            if (userIdStr != null) {
                return UUID.fromString(userIdStr);
            }
        }
        throw new IllegalStateException("Utilisateur non authentifié");
    }
}
package com.king.eschool.Core.jwt;

import org.springframework.stereotype.Service;

import com.king.eschool.Modules.Utilisateurs.Models.Permission;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

private static final String SECRET_KEY = "CHANGE_THIS_SECRET_KEY_CHANGE_THIS_SECRET_KEY_CHANGE_THIS_SECRET_KEY";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 heures

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

   public String generateToken(User user) {
        List<String> authorities = new ArrayList<>();

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // 1. Ajoute le nom du rôle (ex: "ROLE_SUPER_ADMIN" ou son slug)
                authorities.add(role.getSlug());

                // 2. Ajoute toutes les permissions associées à ce rôle (ex: "school:read.all")
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(permission.getSlug()); // ou permission.getSlug() selon votre entité
                    }
                }
            }
        }
        // 🟢 Affiche la liste des autorisations générées dans la console Spring Boot
         System.out.println("Autorisations injectées dans le JWT : " + authorities);
        return Jwts.builder()
                .setClaims(Map.of(
                        "userId", user.getId().toString(),
                        "schoolId", user.getSchoolId() != null ? user.getSchoolId().toString() : "",
                        "campusId", user.getCampusId() != null ? user.getCampusId().toString() : "",
                        "roles", authorities // 🟢 Contient maintenant Rôles ET Permissions
                ))
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSignInKey())
                .compact();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) extractAllClaims(token).get("roles");
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, User user) {
        String email = extractUsername(token);
        return email.equals(user.getEmail()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
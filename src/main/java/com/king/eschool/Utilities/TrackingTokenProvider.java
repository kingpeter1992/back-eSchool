package com.king.eschool.Utilities;

import java.util.Date;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

@Component
public class TrackingTokenProvider {

    private static final String SECRET_KEY = "CHANGE_THIS_SECRET_KEY_CHANGE_THIS_SECRET_KEY_CHANGE_THIS_SECRET_KEY";
    private static final long EXPIRATION_3_MONTHS = 90L * 24 * 60 * 60 * 1000; // 90 jours en millisecondes

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateTrackingToken(String registrationNo) {
        return Jwts.builder()
                .setSubject(registrationNo)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_3_MONTHS))
                .signWith(getSignInKey())
                .compact();
    }

    public String validateAndGetRegistrationNo(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Le lien de suivi est invalide ou expiré.");
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
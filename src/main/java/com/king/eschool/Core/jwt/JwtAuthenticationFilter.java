package com.king.eschool.Core.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.king.eschool.Modules.Utilisateurs.Models.SecurityUser;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
private final JwtService jwtService;
    private final UserRepository userRepository;

@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
) throws ServletException, IOException {

    String path = request.getServletPath();

    // Contournement des routes publiques
    if (path.startsWith("/api/auth/") || path.startsWith("/api/files/")) {
        filterChain.doFilter(request, response);
        return;
    }

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    try {
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null && jwtService.isTokenValid(token, user)) {
                
                // 🟢 1. Extraire les rôles ET permissions intégrés dans le token
                List<String> rolesAndPermissions = jwtService.extractRoles(token);

                // 🟢 2. Les convertir en SimpleGrantedAuthority pour Spring Security
                List<GrantedAuthority> authorities = rolesAndPermissions != null ? 
                        rolesAndPermissions.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList())
                        : Collections.emptyList();

                SecurityUser securityUser = new SecurityUser(user);

                // 🟢 3. Transmettre les autorisations extraites du JWT
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                securityUser,
                                null,
                                authorities // Utilise la liste mixte (Rôles + Permissions)
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    } catch (Exception e) {
        SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
}
}
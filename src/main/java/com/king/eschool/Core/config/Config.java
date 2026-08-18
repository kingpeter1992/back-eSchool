package com.king.eschool.Core.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class Config {

    @Value("${app.front-url}")
    private String appFrontUrl;
// 🟢 Bean de configuration CORS pour autoriser Angular
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Autoriser l'origine de votre Frontend Angular
    configuration.setAllowedOrigins(List.of("http://localhost:4200", appFrontUrl));
    
    // Autoriser les méthodes HTTP usuelles
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    
    // Autoriser tous les en-têtes (Authorization, Content-Type, etc.)
    configuration.setAllowedHeaders(List.of("*"));
    
    // Autoriser l'envoi de cookies/credentials si nécessaire
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

}
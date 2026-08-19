package com.king.eschool.Core.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Collections;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.king.eschool.Core.jwt.JwtAuthenticationFilter;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtFilter;
        private final UserRepository userRepository;
        private final Config config;

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            // 1. Désactiver CSRF (indispensable pour API REST Stateless)
            .csrf(csrf -> csrf.disable())

            // 2. Configuration CORS intégrée
            .cors(cors -> cors.configurationSource(config.corsConfigurationSource()))

            // 3. Gestion des autorisations de requêtes
            .authorizeHttpRequests(auth -> auth
                    // Autoriser toutes les requêtes de pré-vérification CORS (Pre-flight OPTIONS)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Fichiers publics
                    .requestMatchers("/api/files/**").permitAll()
                    .requestMatchers("/api/test/**").permitAll()

                    // Endpoints d'authentification (login, register, forgot-password, reset-password, etc.)
                    .requestMatchers("/api/auth/**").permitAll()

                    // Endpoints réservés à l'administrateur
                    .requestMatchers("/api/v1/**").hasAuthority("ROLE_SUPER_ADMIN")

                    // Tout le reste nécessite une authentification via JWT
                    .anyRequest().authenticated()
            )

            // 4. Gestion de la session (Stateless car JWT)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 5. Provider & Filtre JWT
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}

        @Bean
        public UserDetailsService userDetailsService() {
                        return email -> userRepository.findByEmail(email)
                                                        .map(user -> new org.springframework.security.core.userdetails.User(
                                                                                        user.getEmail(),
                                                                                        user.getPasswordHash(),
                                                                                        Collections.emptyList()))
                                                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

                provider.setUserDetailsService(userDetailsService());
                provider.setPasswordEncoder(passwordEncoder());

                return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
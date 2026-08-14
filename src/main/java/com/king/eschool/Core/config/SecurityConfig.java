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

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                return http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> {
                                })
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/api/files/upload").permitAll()
                                                .requestMatchers("/api/files/**").permitAll()
                                                .requestMatchers(
                                                                 "/api/auth/**",
                                                                "/api/auth/register",
                                                                "/api/auth/login")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
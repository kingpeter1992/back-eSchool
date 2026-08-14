package com.king.eschool.Modules.Utilisateurs.Models;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecurityUser implements UserDetails {

    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Combine les slugs des rôles (ex: ROLE_SCHOOL_ADMIN) et toutes les permissions associées
        return user.getRoles().stream()
                .flatMap(role -> Stream.concat(
                        Stream.of(new SimpleGrantedAuthority(role.getSlug())),
                        role.getPermissions().stream().map(p -> new SimpleGrantedAuthority(p.getSlug()))
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getDeletedAt() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != User.UserStatus.LOCKED && user.getStatus() != User.UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == User.UserStatus.ACTIVE || user.getStatus() == User.UserStatus.PENDING_ACTIVATION;
    }

    public User getUser() {
        return user;
    }
}
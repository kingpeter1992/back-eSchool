package com.king.eschool.Modules.Utilisateurs.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Utilisateurs.Models.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    Optional<User> findByActivationToken(String activationToken);
}

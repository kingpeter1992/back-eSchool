package com.king.eschool.Core.config;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetServiceImpl {

    private final UserRepository userRepository;

    private final EmailServiceImpl emailService;


    public void sendResetLink(String email) {

        log.info("Forgot password demandé pour : {}", email);

        Optional<User> optionalUser =
                userRepository.findByEmail(email);

        /*
         * Pour éviter de révéler si un compte existe,
         * on ne lance pas d'erreur si l'utilisateur
         * n'existe pas.
         */
        if (optionalUser.isEmpty()) {
            log.info("Aucun utilisateur trouvé pour : {}", email);
            return;
        }

        User user = optionalUser.get();

        log.info("Utilisateur trouvé : {}", user.getEmail());

        String token = generateSecureToken();

        user.setResetToken(token);

        user.setResetTokenExpiry(
                LocalDateTime.now().plusMinutes(30)
        );

        userRepository.save(user);

        log.info("Token de réinitialisation enregistré.");

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFirstName(),
                token
        );

        log.info("Demande d'envoi email terminée.");
    }


    private String generateSecureToken() {

        byte[] randomBytes =
                new byte[32];


        new SecureRandom()
                .nextBytes(randomBytes);


        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }


    @Transactional
public void resetPassword(
        String token,
        String newPassword
) {

    User user =
            userRepository
                .findByResetToken(token)
                .orElseThrow(
                    () -> new RuntimeException(
                        "Token invalide ou expiré."
                    )
                );


    if (
        user.getResetTokenExpiry() == null
        ||
        user.getResetTokenExpiry()
            .isBefore(LocalDateTime.now())
    ) {

        throw new RuntimeException(
            "Token invalide ou expiré."
        );
    }

    user.setPasswordHash(
        new BCryptPasswordEncoder()
            .encode(newPassword)
    );


    // Token à usage unique
    user.setResetToken(null);

    user.setResetTokenExpiry(null);

    // On peut également remettre le compte actif
    // si le reset est autorisé sur un compte actif.
    if (
        user.getStatus()
            == User.UserStatus.PENDING_ACTIVATION
    ) {

        user.setStatus(
            User.UserStatus.ACTIVE
        );

        user.setActive(true);
    }


    user.setFailedLoginAttempts(0);


    userRepository.save(user);
}
}
package com.king.eschool.Core.Auth;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Core.config.PasswordResetServiceImpl;
import com.king.eschool.Core.dtoRequest.ActivateAccountRequest;
import com.king.eschool.Core.dtoRequest.LoginRequest;
import com.king.eschool.Core.dtoRequest.ResetPasswordRequest;
import com.king.eschool.Core.dtoResponse.AuthResponse;
import com.king.eschool.Core.dtoResponse.UserResponse;
import com.king.eschool.Modules.Utilisateurs.Dto.request.ActivationContextDto;
import com.king.eschool.Modules.Utilisateurs.Dto.request.CompleteActivationDto;
import com.king.eschool.Modules.Utilisateurs.Dto.request.CreateUserDto;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.ServiceImplement.UserServiceImpl;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl authService;
    private final PasswordResetServiceImpl passwordResetService;


        @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> request) {
                
        String email = request.get("email");
        if (email == null
                ||
                email.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "L'adresse e-mail est obligatoire."));
        }

        passwordResetService
                .sendResetLink(email);

        /*
         * Toujours retourner le même message.
         * Cela évite qu'un attaquant puisse savoir
         * si une adresse existe dans la base.
         */

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Si cette adresse existe, "
                                + "un lien de réinitialisation "
                                + "a été envoyé."));
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserDto dto) {
        User createdUser = authService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(createdUser));
    }


    @PostMapping("/activate")
public ResponseEntity<?> activateAccount(
        @RequestBody ActivateAccountRequest request
) {

    authService.activateAccount(
        request.getToken(),
        request.getPassword()
    );

    return ResponseEntity.ok(
        Map.of(
            "message",
            "Compte activé avec succès."
        )
    );
}

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }



    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody ResetPasswordRequest request) {

                
        passwordResetService.resetPassword(
                request.getToken(),
                request.getPassword());

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Mot de passe réinitialisé avec succès."));
    }


    @GetMapping("/api/v1/auth/verify-activation-token")
public ResponseEntity<ActivationContextDto> verifyToken(@RequestParam("token") String token) {
    User user = authService.findByActivationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Token invalide ou introuvable."));

    if (user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
        throw new IllegalStateException("Le token d'activation a expiré.");
    }

    // Récupération du rôle principal pour déterminer le formulaire
    String primaryRole = user.getRoles().stream()
            .map(Role::getSlug)
            .findFirst()
            .orElse("");

    ActivationContextDto response = ActivationContextDto.builder()
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(primaryRole) // Ex: ROLE_DIRECTEUR, ROLE_PARENT, ROLE_ELEVE, ROLE_ADMIN_ECOLE
            .schoolId(user.getSchoolId())
            .build();

    return ResponseEntity.ok(response);
}


@PostMapping("/api/v1/auth/complete-activation")
public ResponseEntity<Void> completeActivation(@Valid @RequestBody CompleteActivationDto dto) {
    authService.completeUserActivation(dto);
    return ResponseEntity.ok().build();
}

}

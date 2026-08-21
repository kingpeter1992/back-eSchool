package com.king.eschool.Modules.Utilisateurs.Controller;


import lombok.RequiredArgsConstructor; // 👈 Ajouter cet import

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Core.dtoResponse.UserResponse;
import com.king.eschool.Modules.Utilisateurs.Dto.reponse.UserResponseDto;
import com.king.eschool.Modules.Utilisateurs.Dto.request.CreateUserDto;
import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.ServiceImplement.UserServiceImpl;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor // 👈 Génère le constructeur pour userService
public class UserController {

    // 🟢 Bonne pratique : Injectez l'interface (UserService) plutôt que l'implémentation (UserServiceImpl)
    private final UserServiceImpl userService; 

// 🟢 Autorise 'user:read.all' OU les utilisateurs possédant 'ROLE_SUPER_ADMIN'
    @GetMapping
    //@PreAuthorize("hasAuthority('user:read.all') or hasRole('SUPER_ADMIN')")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE'))")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{id}")
 //   @PreAuthorize("hasAuthority('user:delete') or hasRole('SUPER_ADMIN')")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE'))")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    

    @PostMapping()
//   @PreAuthorize("hasAuthority('user:create')")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE'))")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserDto dto) {
        User createdUser = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(createdUser));
    }

}
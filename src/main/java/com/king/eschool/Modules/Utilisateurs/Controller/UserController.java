package com.king.eschool.Modules.Utilisateurs.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.Utilisateurs.Models.User;
import com.king.eschool.Modules.Utilisateurs.ServiceImplement.UserServiceImpl;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

 @GetMapping
    @PreAuthorize("hasAuthority('user:read.all')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

}

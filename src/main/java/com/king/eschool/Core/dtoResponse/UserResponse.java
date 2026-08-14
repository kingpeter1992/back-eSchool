package com.king.eschool.Core.dtoResponse;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.king.eschool.Modules.Utilisateurs.Models.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private UUID schoolId; // Toujours attaché à une école
    private UUID campusId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private User.UserStatus status;
    private int failedLoginAttempts;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .schoolId(user.getSchoolId())
                .campusId(user.getCampusId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .roles(user.getRoles().stream()
                        .map(role -> role.getSlug())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
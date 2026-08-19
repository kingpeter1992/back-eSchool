package com.king.eschool.Modules.Utilisateurs.Dto.reponse;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String status; // PENDING_ACTIVATION, ACTIVE, SUSPENDED, LOCKED
    private UUID schoolId;
    private UUID campusId;
    private String schoolName;
    private SchoolInfo school;
    private Set<RoleResponseDto> roles;
    private Set<PermissionResponseDto> permissions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SchoolInfo {
        private UUID id;
        private String name;
        private String code;
        private String email;
        private String phone;
        private String logoUrl;
        private String currency;
        private String timezone;
        private String domain;
        private String status;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleResponseDto {
        private UUID id;
        private String name;
        private String slug;
        private boolean system;
        private Set<PermissionResponseDto> permissions;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PermissionResponseDto {
        private UUID id;
        private String name;
        private String slug;
        private String category;
    }
}
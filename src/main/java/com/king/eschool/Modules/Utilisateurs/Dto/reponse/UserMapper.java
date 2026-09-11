package com.king.eschool.Modules.Utilisateurs.Dto.reponse;
import org.springframework.stereotype.Component;

import com.king.eschool.Modules.School.Models.Campus;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.Utilisateurs.Models.Permission;
import com.king.eschool.Modules.Utilisateurs.Models.Role;
import com.king.eschool.Modules.Utilisateurs.Models.User;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
public UserResponseDto toResponseDto(User user, School school, Campus campus) {
        if (user == null) return null;

        Set<UserResponseDto.RoleResponseDto> roleDtos = user.getRoles() != null
                ? user.getRoles().stream().map(this::toRoleDto).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<UserResponseDto.PermissionResponseDto> allPermissions = user.getRoles() != null
                ? user.getRoles().stream()
                        .filter(role -> role.getPermissions() != null)
                        .flatMap(role -> role.getPermissions().stream())
                        .map(this::toPermissionDto)
                        .collect(Collectors.toSet())
                : Collections.emptySet();

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .schoolId(user.getSchoolId())
                .campusId(user.getCampusId())
                .schoolName(school != null ? school.getName() : null)
                .school(toSchoolInfo(school))
                .campus(toCampusInfo(campus)) // 🟢 Mapping du Campus
                .roles(roleDtos)
                .permissions(allPermissions)
                .build();
    }

    public UserResponseDto.SchoolInfo toSchoolInfo(School school) {
        if (school == null) return null;
        return UserResponseDto.SchoolInfo.builder()
                .id(school.getId())
                .name(school.getName())
                .code(school.getCode())
                .email(school.getEmail())
                .phone(school.getPhone())
                .logoUrl(school.getLogoUrl())
                .currency(school.getCurrency())
                .timezone(school.getTimezone())
                .domain(school.getDomain())
                .status(school.getStatus() != null ? school.getStatus().name() : null)
                .build();
    }

    public UserResponseDto.CampusInfo toCampusInfo(Campus campus) {
        if (campus == null) return null;
        return UserResponseDto.CampusInfo.builder()
                .id(campus.getId())
                .name(campus.getName())
                .code(campus.getCode())
                .address(campus.getAddress())
                .city(campus.getCity())
                .phone(campus.getPhone())
                .email(campus.getEmail())
//                .mainCampus(campus.isMainCampus())
                .build();
    }

    private UserResponseDto.RoleResponseDto toRoleDto(Role role) {
        if (role == null) return null;

        Set<UserResponseDto.PermissionResponseDto> permissionDtos = role.getPermissions() != null
                ? role.getPermissions().stream().map(this::toPermissionDto).collect(Collectors.toSet())
                : Collections.emptySet();

        return UserResponseDto.RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .slug(role.getSlug())
                .system(role.isSystem())
                .permissions(permissionDtos)
                .build();
    }

    private UserResponseDto.PermissionResponseDto toPermissionDto(Permission permission) {
        if (permission == null) return null;
        return UserResponseDto.PermissionResponseDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .slug(permission.getSlug())
                .category(permission.getCode())
                .build();
    }
}
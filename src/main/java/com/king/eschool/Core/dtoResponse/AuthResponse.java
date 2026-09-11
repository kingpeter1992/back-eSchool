package com.king.eschool.Core.dtoResponse;


import java.util.List;
import java.util.Set;

import com.king.eschool.Modules.Utilisateurs.Dto.reponse.UserResponseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
private String token;
    private String refreshToken;
    private UserResponseDto user;
    private UserResponseDto.SchoolInfo school; // 🟢 École de l'utilisateur
    private UserResponseDto.CampusInfo campus; // 🟢 Campus de l'utilisateur
    private String role;
    private List<String> roles;
    private Set<String> permissions;
}

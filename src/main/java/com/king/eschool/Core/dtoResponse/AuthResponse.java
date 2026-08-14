package com.king.eschool.Core.dtoResponse;


import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;

    private String refreshToken;

    private UserAuthInfo user;

    private SchoolInfo school;

    private List<String> permissions;
}

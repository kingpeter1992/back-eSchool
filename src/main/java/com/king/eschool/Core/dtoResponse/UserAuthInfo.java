package com.king.eschool.Core.dtoResponse;

import java.util.List;
import java.util.UUID;

import lombok.*;

@Data
@Builder
public class UserAuthInfo {

    private UUID id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private String status;

    private List<String> roles;
}
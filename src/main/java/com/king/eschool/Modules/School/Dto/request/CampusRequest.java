package com.king.eschool.Modules.School.Dto.request;

import com.king.eschool.Modules.School.Models.School;

import jakarta.validation.constraints.NotBlank;

public record CampusRequest(
        @NotBlank(message = "Le nom du campus est obligatoire")
        String name,
        String address,
        String phone,
        School school
) {}

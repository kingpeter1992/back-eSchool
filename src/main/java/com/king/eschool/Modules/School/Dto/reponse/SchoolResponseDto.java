package com.king.eschool.Modules.School.Dto.reponse;



import java.util.UUID;

import com.king.eschool.Modules.School.Models.School;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolResponseDto {
    private UUID id;
    private String name;
    private String code;
    private String email;
    private String phone;
    private String logoUrl;
    private String currency;
    private String timezone;
    private String domain;
    private School.SchoolStatus status; // Doit correspondre au type de l'entité

}
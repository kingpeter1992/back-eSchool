package com.king.eschool.Modules.School.Dto.reponse;



import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.king.eschool.Modules.School.Dto.SchoolStatus;
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
    private SchoolStatus status; // Doit correspondre au type de l'entité
    List<CampusResponse> campuses;
    LocalDateTime createdAt;

}
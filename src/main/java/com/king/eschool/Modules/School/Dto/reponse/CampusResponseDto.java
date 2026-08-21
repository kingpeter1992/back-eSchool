package com.king.eschool.Modules.School.Dto.reponse;

import java.util.UUID;

import com.king.eschool.Modules.School.Models.Campus.CampusStatus;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CampusResponseDto {
    private UUID id;
    private UUID schoolId;
    private String schoolName;
    private String name;
    private String code;
    private String address;
    private String city;
    private String country;
    private String phone;
    private CampusStatus status;
    private LocalDateTime createdAt;
}
package com.king.eschool.Core.dtoResponse;

import java.util.UUID;


import lombok.*;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolInfo {

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
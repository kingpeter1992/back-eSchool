package com.king.eschool.Modules.School.Dto.reponse;

import java.util.UUID;

public record CampusResponse(
        UUID id,
        String name,
        String address,
        String phone
) {}

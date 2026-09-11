package com.king.eschool.Modules.School.Dto.request;

import java.util.List;

public record CampusScheduleRequest(
    String campusId,
    List<ScheduleDTO> schedules
) {}
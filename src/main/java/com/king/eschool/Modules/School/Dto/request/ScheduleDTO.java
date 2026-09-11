package com.king.eschool.Modules.School.Dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record ScheduleDTO(
    String id,
    DayOfWeek dayOfWeek,
    boolean isOpen,

    @JsonFormat(pattern = "HH:mm")
    LocalTime morningStartTime,

    @JsonFormat(pattern = "HH:mm")
    LocalTime morningEndTime,

    @JsonFormat(pattern = "HH:mm")
    LocalTime eveningStartTime,

    @JsonFormat(pattern = "HH:mm")
    LocalTime eveningEndTime
) {}
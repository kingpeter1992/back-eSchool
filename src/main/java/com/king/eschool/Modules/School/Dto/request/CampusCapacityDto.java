package com.king.eschool.Modules.School.Dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CampusCapacityDto {
    @NotNull @Min(1)
    private Integer totalCapacity;
    UUID classId;
    String className;
    String levelName;
    String campusName;
    int maxCapacity;
    int currentStudents;
    int availableSeats;
    boolean isFull;
}

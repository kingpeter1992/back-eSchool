package com.king.eschool.Modules.Admission.Dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
public class ClassCapacityDto {
    private UUID id;
    private String name;
    private String levelName;
    private UUID campusId;
    private int maxCapacity;
    private int currentCount;
    private int availableSeats;
    private boolean isFull;
}
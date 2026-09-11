package com.king.eschool.Modules.School.Dto.request;

import java.util.UUID;

import com.king.eschool.Modules.School.Models.Campus.CampusStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class ChangeCampusStatusDto {
    @NotNull(message = "Le statut est obligatoire")
    private CampusStatus status;
}




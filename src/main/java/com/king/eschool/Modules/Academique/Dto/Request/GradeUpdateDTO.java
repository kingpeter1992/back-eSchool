package com.king.eschool.Modules.Academique.Dto.Request;

public record GradeUpdateDTO(
    String periodId,
    String studentId,
    Double value,
    String updatedBy
) {}
package com.king.eschool.Modules.School.Dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendEmailRequestDto {
    @NotBlank(message = "Le sujet est obligatoire")
    private String subject;

    @NotBlank(message = "Le message est obligatoire")
    private String message;
}
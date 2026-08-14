package com.king.eschool.Core.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivateAccountRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
}
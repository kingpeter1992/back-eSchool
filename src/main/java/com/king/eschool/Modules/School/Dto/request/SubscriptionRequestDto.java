package com.king.eschool.Modules.School.Dto.request;


import lombok.Data;
import com.king.eschool.Modules.School.Models.Subscription.PlanType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class SubscriptionRequestDto {
    @NotNull(message = "Le type de plan est obligatoire")
    private PlanType planType;

    @NotNull(message = "La durée est obligatoire")
    @Positive(message = "La durée doit être supérieure à 0")
    private Integer durationInMonths;

    @NotNull(message = "Le montant est obligatoire")
    private BigDecimal amount;

    private String currency = "USD";
}
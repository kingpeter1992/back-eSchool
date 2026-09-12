package com.king.eschool.Modules.Admission.Models;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfoDTO {

   @NotNull(message = "Le montant payé est requis")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à zéro")
    private BigDecimal amountPaid;

    @NotBlank(message = "Le mode de paiement est requis")
    private String paymentMethod; // MOBILE_MONEY, BANK_TRANSFER, CASH, etc.

    @NotBlank(message = "Le numéro de compte ou téléphone de paiement est requis")
    private String paymentPhoneOrCard;

    private String paymentReference;
    private String paymentReason;
    private String paymentDate;
    private UUID schoolId;
    private UUID campusId;
}
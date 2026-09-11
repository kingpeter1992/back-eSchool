package com.king.eschool.Modules.Admission.Dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateEnrollmentRequestDTO {
    @NotNull(message = "L'école est obligatoire")
    private UUID schoolId;

    @NotNull(message = "Le campus est obligatoire")
    private UUID campusId;

    @NotNull(message = "L'année scolaire est obligatoire")
    private UUID academicYearId;

    private UUID studentId; // Optionnel si élève existant

    // Requis si studentId est null
    @NotNull(message = "L'année scolaire est obligatoire")
    private String candidateFirstName;
    @NotNull(message = "L'année scolaire est obligatoire")

    private String candidateLastName;
    private LocalDate candidateDateOfBirth;

    private List<String> documentTypes;

    private UUID parentUserId;
    @NotNull(message = "L'année scolaire est obligatoire")
    @Email
    private String candidateEmail;
    @NotNull(message = "L'année scolaire est obligatoire")
    private String candidatePhone;

    private String photoUrl; // URL renvoyée par Supabase Storage

    // Simulation de paiement
    @NotBlank
    private String paymentMethod; // "VISA" ou "MOBILE_MONEY"
    private String paymentReference;
    private Double amountPaid;

}

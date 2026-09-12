package com.king.eschool.Modules.Admission.Dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.king.eschool.Modules.Admission.Models.EnrollmentDocument;
import com.king.eschool.Modules.Admission.Models.PaymentInfoDTO;

@Data
public class CreateEnrollmentRequestDTO {
// ============================================================
    // CONTEXTE
    // ============================================================
    @NotNull(message = "L'ID de l'école est requis")
    private UUID schoolId;

    @NotNull(message = "L'ID du campus est requis")
    private UUID campusId;

    @NotNull(message = "L'ID de l'année académique est requis")
    private UUID academicYearId;

    // ============================================================
    // ÉLÈVE / CANDIDAT (INFORMATIONS PERSONNELLES & ÉTAT CIVIL)
    // ============================================================
    @NotBlank(message = "Le nom du candidat est requis")
    private String candidateLastName;

    private String candidatePostName;

    @NotBlank(message = "Le prénom du candidat est requis")
    private String candidateFirstName;

    @NotBlank(message = "Le genre est requis")
    private String gender;

    @NotNull(message = "La date de naissance est requise")
    private LocalDate candidateDateOfBirth;

    private String placeOfBirth;

    @NotBlank(message = "Le téléphone du candidat est requis")
    private String candidatePhone;

    @NotBlank(message = "L'email du candidat est requis")
    @Email(message = "Le format de l'adresse email est invalide")
    private String candidateEmail;

    private String address;
    private String city;
    private String maritalStatus;
    private String nationality;
    private String originVillage;
    private String district;
    private String territory;

    // ============================================================
    // RESPONSABLE LÉGAL / PARENT
    // ============================================================
    private String parentFullName;
    private String parentPhone;
    private String parentAddress;

    // ============================================================
    // SCOLARITÉ & ORIENTATION
    // ============================================================
    @NotBlank(message = "La vacation est requise")
    private String vacation;

    @NotNull(message = "Le cycle est requis")
    private UUID cycleId;

    @NotNull(message = "Le niveau d'études est requis")
    private UUID levelId;

    private UUID sectionId;
    private UUID optionId;

    private String targetClass;
    private String previousSchool;
    private BigDecimal previousPercentage;

    // Métadonnées sur les types de documents joints
    private List<String> documentTypes;

    // ============================================================
    // PAIEMENT (DTO DÉDIÉ)
    // ============================================================
    @Valid
    private PaymentInfoDTO paymentInfo;
}

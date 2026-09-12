package com.king.eschool.Modules.Admission.Dto.response;

import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentResponseDTO {

    private UUID id;
    private String registrationNo;
    private EnrollmentStatus status;
    private String photoUrl;
    private String remarks;

    // Contexte
    private UUID schoolId;
    private String schoolName;
    private UUID campusId;
    private String campusName;
    private UUID academicYearId;
    private String academicYearName;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private UUID classId;
    private String className;

    // Candidat
    private String candidateFirstName;
    private String candidatePostName;
    private String candidateLastName;
    private String gender;
    private LocalDate candidateDateOfBirth;
    private String placeOfBirth;
    private String candidatePhone;
    private String candidateEmail;
    private String address;
    private String city;
    private String maritalStatus;
    private String nationality;
    private String originVillage;
    private String district;
    private String territory;

    // Parent
    private UUID parentUserId;
    private String parentFullName;
    private String parentPhone;
    private String parentAddress;

    // Orientation
    private String vacation;
    private UUID cycleId;
    private UUID levelId;
    private UUID sectionId;
    private UUID optionId;
    private String targetClass;
    private String previousSchool;
    private BigDecimal previousPercentage;

    // Règlement
    private String paymentReference;
    private String paymentMethod;
    private String paymentPhoneOrCard;
    private BigDecimal amountPaid;

    // Documents & Dates
    private List<DocumentResponseDTO> documents;
    private LocalDate admissionDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentResponseDTO {
        private UUID id;
        private String documentType;
        private String fileName;
        private String fileType;
        private String fileUrl;
    }
}
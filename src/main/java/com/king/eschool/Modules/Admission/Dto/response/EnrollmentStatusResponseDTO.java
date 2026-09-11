package com.king.eschool.Modules.Admission.Dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentStatusResponseDTO {
    private String registrationNo;
    private String candidateFullName;
    private String submissionDate;
    private String schoolName;
    private String campusName;
    private String status;       // PENDING, UNDER_REVIEW, ACCEPTED, REJECTED
    private String statusLabel;  // Libellé en français (ex: "En attente", "Accepté")
    private Integer stepNumber;  // 1 (Soumis), 2 (En révision), 3 (Décision)
    private String remarks;      // Motif ou note de l'administration
}
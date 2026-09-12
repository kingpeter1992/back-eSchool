
package com.king.eschool.Modules.Admission.Models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_enrollment_school_campus_student_year", columnNames = {
                "school_id", "campus_id", "student_id", "academic_year_id"
        }),
        @UniqueConstraint(name = "uk_enrollment_registration_no", columnNames = "registration_no")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "campus_id", nullable = false)
    private UUID campusId;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "parent_user_id")
    private UUID parentUserId;

    @Column(name = "class_id")
    private UUID classId;

    private String remarks;
    private String photoUrl;

    @Column(name = "registration_no", length = 50, unique = true)
    private String registrationNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    // ============================================================
    // ÉLÈVE / CANDIDAT (INFORMATIONS PERSONNELLES & ÉTAT CIVIL)
    // ============================================================
    @Column(name = "candidate_first_name")
    private String candidateFirstName;

    @Column(name = "candidate_post_name")
    private String candidatePostName;

    @Column(name = "candidate_last_name")
    private String candidateLastName;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "candidate_dob")
    private LocalDate candidateDateOfBirth;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "candidate_phone")
    private String candidatePhone;

    @Column(name = "candidate_email")
    private String candidateEmail;

    private String address;
    private String city;

    @Column(name = "marital_status")
    private String maritalStatus;

    private String nationality;

    @Column(name = "origin_village")
    private String originVillage;

    private String district;
    private String territory;

    // ============================================================
    // PARENT / RESPONSABLE LÉGAL
    // ============================================================
    @Column(name = "parent_full_name")
    private String parentFullName;

    @Column(name = "parent_phone")
    private String parentPhone;

    @Column(name = "parent_address")
    private String parentAddress;

    // ============================================================
    // SCOLARITÉ & ORIENTATION
    // ============================================================
    private String vacation; // JOUR, SOIR, etc.

    @Column(name = "cycle_id")
    private UUID cycleId;

    @Column(name = "level_id")
    private UUID levelId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "option_id")
    private UUID optionId;

    @Column(name = "target_class")
    private String targetClass;

    @Column(name = "previous_school")
    private String previousSchool;

    @Column(name = "previous_percentage")
    private BigDecimal previousPercentage;

    // // ============================================================
    // // PAIEMENT & AUDIT
    // // ============================================================
    // @Column(name = "payment_reference")
    // private String paymentReference;

    // @Column(name = "payment_method")
    // private String paymentMethod;

    // @Column(name = "amount_paid")
    // private BigDecimal amountPaid;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EnrollmentDocument> documents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDate.now();
    }
}
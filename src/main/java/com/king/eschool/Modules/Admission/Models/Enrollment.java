
package com.king.eschool.Modules.Admission.Models;

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

    // Peut être nul au moment de la candidature (créé au statut ENROLLED)
    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "parent_user_id")
    private UUID parentUserId;

    @Column(name = "class_id")
    private UUID classId;

    private String remarks; // Motif ou note de l'administration

    private String photoUrl;
    private String candidateEmail;
    private String candidatePhone;

    @Column(name = "registration_no", length = 50, unique = true)
    private String registrationNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    // Informations candidat si l'élève n'est pas encore créé dans le système
    @Column(name = "candidate_first_name")
    private String candidateFirstName;

    @Column(name = "candidate_last_name")
    private String candidateLastName;

    @Column(name = "candidate_dob")
    private LocalDate candidateDateOfBirth;

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
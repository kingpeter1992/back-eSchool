package com.king.eschool.Modules.Admission.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.king.eschool.Modules.Admission.Models.Enrollment;
import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;



@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findBySchoolId(UUID schoolId);

    List<Enrollment> findBySchoolIdAndCampusId(UUID schoolId, UUID campusId);

    List<Enrollment> findBySchoolIdAndStatus(UUID schoolId, EnrollmentStatus status);

    List<Enrollment> findByStudentId(UUID studentId);

    List<Enrollment> findByAcademicYearId(UUID academicYearId);

    List<Enrollment> findBySchoolIdAndAcademicYearId(UUID schoolId, UUID academicYearId);

    Optional<Enrollment> findByStatus(EnrollmentStatus status);

    Optional<Enrollment> findByStudentIdAndSchoolIdAndAcademicYearId(
            UUID studentId,
            UUID schoolId,
            UUID academicYearId
    );

    boolean existsByRegistrationNo(String registrationNo);

    boolean existsByStudentIdAndAcademicYearId(UUID studentId, UUID academicYearId);

    boolean existsBySchoolIdAndStudentIdAndAcademicYearId(
            UUID schoolId,
            UUID studentId,
            UUID academicYearId
    );

    boolean existsByStudentIdAndSchoolIdAndAcademicYearId(
            UUID studentId,
            UUID schoolId,
            UUID academicYearId
    );

    boolean existsBySchoolIdAndCampusIdAndStudentIdAndAcademicYearId(
            UUID schoolId, 
            UUID campusId, 
            UUID studentId, 
            UUID academicYearId
    );

    long countBySchoolId(UUID schoolId);

    long countBySchoolIdAndStatus(UUID schoolId, EnrollmentStatus status);

    // ✅ Methode valide pour compter par classe et année
    int countByClassIdAndAcademicYearId(UUID classId, UUID academicYearId);

    Optional<Enrollment> findByRegistrationNo(String registrationNo);


    @Query(value = "SELECT nextval('enrollment_reg_no_seq')", nativeQuery = true)
    Long getNextRegistrationSequence();

    
}
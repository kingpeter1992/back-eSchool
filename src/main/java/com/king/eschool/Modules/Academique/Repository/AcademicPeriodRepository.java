package com.king.eschool.Modules.Academique.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Models.AcademicPeriod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, String> {

    List<AcademicPeriod> findByAcademicYearId(UUID academicYearId);

    // ✅ Modifier le type de retour de AcademicPeriodDTO à AcademicPeriod
    List<AcademicPeriod> findBySchoolIdAndAcademicYearIdAndAcademicTermIdOrderByStartDateAsc(
            UUID schoolId,
            UUID academicYearId, 
            UUID academicTermId
    );

    List<AcademicPeriod> findBySchoolIdAndAcademicYearIdOrderByStartDateAsc(
            UUID schoolId, 
            UUID academicYearId
    );

    Optional<AcademicPeriod> findByIdAndSchoolId(UUID id, UUID schoolId);

    boolean existsBySchoolIdAndAcademicTermIdAndCodeIgnoreCase(UUID schoolId, UUID academicTermId, String trim);
}
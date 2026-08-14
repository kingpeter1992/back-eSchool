package com.king.eschool.Modules.Academique.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Models.AcademicYear;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    List<AcademicYear> findBySchoolId(UUID schoolId);
    Optional<AcademicYear> findBySchoolIdAndIsCurrentTrue(UUID schoolId);
}

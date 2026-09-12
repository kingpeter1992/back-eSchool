package com.king.eschool.Modules.Academique.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicYearDTO;
import com.king.eschool.Modules.Academique.Enum.AcademicYearStatus;
import com.king.eschool.Modules.Academique.Models.AcademicYear;

import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    Optional<AcademicYear> findBySchoolIdAndIsCurrentTrue(UUID schoolId);
    Optional<AcademicYear> findBySchoolIdAndStatus(UUID schoolId, String active);
    Optional<AcademicYear> findById(UUID yearId);
    List<AcademicYearDTO> findBySchoolIdOrderByStartDateDesc(UUID schoolId);
    Optional<AcademicYear> findByIdAndSchoolId(UUID academicYearId, UUID schoolId);
    boolean existsBySchoolIdAndNameIgnoreCase(UUID schoolId, String trim);
    @Modifying
    @Transactional 
    @Query("""
        UPDATE AcademicYear a
        SET a.status = :newStatus,
            a.isCurrent = false
        WHERE a.schoolId = :schoolId
    """)
    int deactivateAllBySchoolId(
            @Param("schoolId") UUID schoolId,
            @Param("newStatus") AcademicYearStatus newStatus);

            // ✅ Retourne l'entité AcademicYear
@Query("SELECT y FROM AcademicYear y WHERE y.schoolId = :schoolId")
List<AcademicYear> findBySchoolId(@Param("schoolId") UUID schoolId);

}

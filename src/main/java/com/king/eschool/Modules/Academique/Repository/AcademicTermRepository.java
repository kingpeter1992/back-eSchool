package com.king.eschool.Modules.Academique.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicTermDTO;
import com.king.eschool.Modules.Academique.Models.AcademicTerm;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicTermRepository
        extends JpaRepository<AcademicTerm, UUID> {

    /**
     * Tous les trimestres / semestres
     * d'une année scolaire et d'une école.
     */
    List<AcademicTerm> findBySchoolIdAndAcademicYearIdOrderByStartDateAsc(
            UUID schoolId,
            UUID academicYearId
    );

    /**
     * Récupérer un trimestre / semestre
     * en vérifiant son appartenance à l'école.
     */
    Optional<AcademicTerm> findByIdAndSchoolId(
            UUID id,
            UUID schoolId
    );

    /**
     * Récupérer un trimestre / semestre
     * par école + année scolaire.
     */
    Optional<AcademicTerm> findByIdAndSchoolIdAndAcademicYearId(
            UUID id,
            UUID schoolId,
            UUID academicYearId
    );

    /**
     * Vérifier si un code existe déjà
     * dans une année scolaire pour une école.
     */
    boolean existsBySchoolIdAndAcademicYearIdAndCode(
            UUID schoolId,
            UUID academicYearId,
            String code
    );

    /**
     * Vérifier si un nom existe déjà
     * dans une année scolaire pour une école.
     */
    boolean existsBySchoolIdAndAcademicYearIdAndName(
            UUID schoolId,
            UUID academicYearId,
            String name
    );

    List<AcademicTermDTO> findByAcademicYearIdAndSchoolIdOrderByStartDateAsc(UUID academicYearId, UUID schoolId);

    boolean existsBySchoolIdAndAcademicYearIdAndCodeIgnoreCase(UUID schoolId, UUID academicYearId, String trim);

// ✅ Utilisez t.schoolId et t.academicYearId (pas t.school.id)
    @Query("SELECT t FROM AcademicTerm t WHERE t.schoolId = :schoolId AND t.academicYearId = :academicYearId")
    List<AcademicTerm> findBySchoolIdAndAcademicYearId(
            @Param("schoolId") UUID schoolId, 
            @Param("academicYearId") UUID academicYearId
    );

}
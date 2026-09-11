package com.king.eschool.Modules.Academique.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Models.AcademicLevel;

@Repository
public interface AcademicLevelRepository extends JpaRepository<AcademicLevel, UUID> {
    List<AcademicLevel> findByCycleSchoolId(UUID schoolId);

    boolean existsByCycleSchoolIdAndNameIgnoreCase(UUID schoolId, String levelName);

    // Vérifie si le niveau contient des classes qui ne sont pas supprimées (deleted = false)
    @Query("SELECT COUNT(c) > 0 FROM SchoolClass c WHERE c.level.id = :levelId AND c.deleted = false")
    boolean hasActiveClasses(@Param("levelId") UUID levelId);
}
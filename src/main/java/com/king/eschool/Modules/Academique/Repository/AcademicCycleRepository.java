package com.king.eschool.Modules.Academique.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Models.AcademicCycle;

@Repository
public interface AcademicCycleRepository extends JpaRepository<AcademicCycle, UUID> {
    List<AcademicCycle> findBySchoolId(UUID schoolId);


    @Query("SELECT DISTINCT c FROM AcademicCycle c " +
           "LEFT JOIN FETCH c.levels l " +
           "LEFT JOIN FETCH c.sections s " +
           "LEFT JOIN FETCH s.options o " +
           "WHERE c.schoolId = :schoolId " +
           "ORDER BY c.name ASC")
    List<AcademicCycle> findBySchoolIdWithFullTree(@Param("schoolId") String schoolId);
}
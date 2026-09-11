package com.king.eschool.Modules.Academique.Repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Models.AcademicSection;
@Repository
public interface AcademicSectionRepository extends JpaRepository<AcademicSection, java.util.UUID> {
    List<AcademicSection> findByCycleId(UUID cycleId);
    boolean existsByCycleIdAndNameIgnoreCase(UUID cycleId, String sectionName);
    Optional<AcademicSection> findById(java.util.UUID sectionId);
    
}

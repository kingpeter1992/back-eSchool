package com.king.eschool.Modules.Academique.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Models.AcademicOption;


@Repository
public interface AcademicOptionRepository extends JpaRepository<AcademicOption, UUID> {
    List<AcademicOption> findBySectionId(UUID sectionId);
    boolean existsBySectionIdAndNameIgnoreCase(UUID sectionId, String optionName);
    boolean existsBySectionCycleSchoolIdAndCodeIgnoreCase(UUID schoolId, String code);
    
}

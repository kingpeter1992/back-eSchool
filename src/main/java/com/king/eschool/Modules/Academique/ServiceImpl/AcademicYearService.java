package com.king.eschool.Modules.Academique.ServiceImpl;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Modules.Academique.Models.AcademicYear;
import com.king.eschool.Modules.Academique.Repository.AcademicYearRepository;

import java.util.List;
import java.util.UUID;

@Service
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    public AcademicYearService(AcademicYearRepository academicYearRepository) {
        this.academicYearRepository = academicYearRepository;
    }

    @Transactional
    public AcademicYear createAcademicYear(AcademicYear academicYear) {
        // Si l'année est marquée comme courante, on désactive les autres pour cette école
        if (academicYear.isCurrent()) {
            deactivateCurrentYear(academicYear.getSchoolId());
            academicYear.setStatus(AcademicYear.YearStatus.ACTIVE);
        }
        return academicYearRepository.save(academicYear);
    }

    @Transactional
    public void activateYear(UUID id, UUID schoolId) {
        deactivateCurrentYear(schoolId);
        
        AcademicYear year = academicYearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Année académique introuvable"));
        
        year.setCurrent(true);
        year.setStatus(AcademicYear.YearStatus.ACTIVE);
        academicYearRepository.save(year);
    }

    private void deactivateCurrentYear(UUID schoolId) {
        academicYearRepository.findBySchoolIdAndIsCurrentTrue(schoolId).ifPresent(current -> {
            current.setCurrent(false);
            academicYearRepository.save(current);
        });
    }

    public List<AcademicYear> getYearsBySchool(UUID schoolId) {
        return academicYearRepository.findBySchoolId(schoolId);
    }
}

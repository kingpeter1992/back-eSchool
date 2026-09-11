package com.king.eschool.Modules.Academique.ServiceImpl;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicYearDTO;
import com.king.eschool.Modules.Academique.Enum.AcademicYearStatus;
import com.king.eschool.Modules.Academique.Models.AcademicYear;
import com.king.eschool.Modules.Academique.Repository.AcademicYearRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class AcademicYearService {

private final AcademicYearRepository yearRepository;
    public AcademicYearService(AcademicYearRepository yearRepository) {
        this.yearRepository = yearRepository;
    }

    @Transactional
    public AcademicYearDTO createYear(UUID schoolId, AcademicYearDTO dto) {
        // RG-AY-002: Fin postérieure au début
        if (dto.endDate().isBefore(dto.startDate()) || dto.endDate().isEqual(dto.startDate())) {
            throw new RuntimeException("La date de fin doit être strictement postérieure à la date de début.");
        }

        AcademicYear year = AcademicYear.builder()
                .schoolId(schoolId)
                .name(dto.name())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .status(AcademicYearStatus.PREPARATION)
                .build();

        return mapToDTO(yearRepository.save(year));
    }

    @Transactional
    public AcademicYearDTO activateYear(UUID schoolId, UUID yearId) {
        // RG-AY-001: Une seule année ACTIVE autorisée par école
        yearRepository.findBySchoolIdAndStatus(schoolId, AcademicYearStatus.ACTIVE)
                .ifPresent(activeYear -> {
                    if (!activeYear.getId().equals(yearId)) {
                        activeYear.setStatus(AcademicYearStatus.CLOSED);
                        yearRepository.save(activeYear);
                    }
                });

        AcademicYear targetYear = yearRepository.findById(yearId)
                .orElseThrow(() -> new RuntimeException("Année scolaire introuvable."));

        targetYear.setStatus(AcademicYearStatus.ACTIVE);
        return mapToDTO(yearRepository.save(targetYear));
    }

    public List<AcademicYearDTO> getYearsBySchool(UUID schoolId) {
        return yearRepository.findBySchoolId(schoolId).stream().map(this::mapToDTO).toList();
    }

    private AcademicYearDTO mapToDTO(AcademicYear entity) {
        return new AcademicYearDTO(
                entity.getId(), entity.getSchoolId(), entity.getName(),
                entity.getStartDate(), entity.getEndDate(), entity.getStatus()
        );
    }

public AcademicYearDTO getActiveYearBySchool(UUID schoolId) {
        // Option 1 : Récupération par status ACTIVE
        return yearRepository.findBySchoolIdAndStatus(schoolId, AcademicYearStatus.ACTIVE)
                .map(this::mapToDTO)
                .or(() -> yearRepository.findBySchoolIdAndIsCurrentTrue(schoolId).map(this::mapToDTO))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune année académique active trouvée pour l'établissement : " + schoolId));
    }

}

package com.king.eschool.Modules.Academique.ServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicPeriodDTO;
import com.king.eschool.Modules.Academique.Dto.Request.GradeUpdateDTO;
import com.king.eschool.Modules.Academique.Enum.AcademicPeriodStatus;
import com.king.eschool.Modules.Academique.Models.AcademicPeriod;
import com.king.eschool.Modules.Academique.Models.AcademicYear;
import com.king.eschool.Modules.Academique.Repository.AcademicPeriodRepository;
import com.king.eschool.Modules.Academique.Repository.AcademicYearRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademicPeriodService {

    private final AcademicPeriodRepository periodRepository;
    private final AcademicYearRepository yearRepository;

    @Transactional
    public AcademicPeriodDTO createPeriod(AcademicPeriodDTO dto) {
        AcademicYear year = yearRepository.findById(dto.academicYearId())
                .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée."));

        // RG-PER-001: Dates contenues dans l'année scolaire parente
        if (dto.startDate().isBefore(year.getStartDate()) || dto.endDate().isAfter(year.getEndDate())) {
            throw new RuntimeException("Les dates de la période doivent s'inscrire strictement dans les limites de l'année scolaire.");
        }

        AcademicPeriod period = AcademicPeriod.builder()
                .academicYear(year)
                .name(dto.name())
                .code(dto.code())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .status(AcademicPeriodStatus.UPCOMING)
                .build();

        return mapToDTO(periodRepository.save(period));
    }

    @Transactional
    public void submitGrade(GradeUpdateDTO dto) {
        AcademicPeriod period = periodRepository.findById(dto.periodId())
                .orElseThrow(() -> new RuntimeException("Période introuvable."));

        // RG-PER-004 & F-003: Bloquer la saisie si la période est CLOSED ou LOCKED
        if (period.getStatus() == AcademicPeriodStatus.CLOSED || period.getStatus() == AcademicPeriodStatus.LOCKED) {
            throw new RuntimeException("La saisie est impossible: La période est fermée ou verrouillée par l'administration.");
        }

        boolean isLateSubmission = LocalDate.now().isAfter(period.getEndDate());
        if (isLateSubmission) {
            // Log d'audit de la saisie tardive (RG-PER-003)
            System.out.printf("[AUDIT] Saisie tardive de note par %s pour l'élève %s le %s%n",
                    dto.updatedBy(), dto.studentId(), LocalDate.now());
        }

        // Sauvegarde de la note dans le système...
    }

public List<AcademicPeriodDTO> getPeriodsByYear(String academicYearIdStr) {
    UUID academicYearId = UUID.fromString(academicYearIdStr);
    
    return periodRepository.findByAcademicYearId(academicYearId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
}

    private AcademicPeriodDTO mapToDTO(AcademicPeriod entity) {
        return new AcademicPeriodDTO(
                entity.getId(), entity.getAcademicYear().getId(),
                entity.getName(), entity.getCode(),
                entity.getStartDate(), entity.getEndDate(), entity.getStatus()
        );
    }
}

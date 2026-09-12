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
import com.king.eschool.Modules.Academique.Models.AcademicTerm;
import com.king.eschool.Modules.Academique.Models.AcademicYear;
import com.king.eschool.Modules.Academique.Repository.AcademicPeriodRepository;
import com.king.eschool.Modules.Academique.Repository.AcademicTermRepository;
import com.king.eschool.Modules.Academique.Repository.AcademicYearRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AcademicPeriodService {

    private final AcademicPeriodRepository periodRepository;
    private final AcademicTermRepository termRepository;
    private final AcademicYearRepository yearRepository;

    public AcademicPeriodService(
            AcademicPeriodRepository periodRepository,
            AcademicTermRepository termRepository,
            AcademicYearRepository yearRepository
    ) {
        this.periodRepository = periodRepository;
        this.termRepository = termRepository;
        this.yearRepository = yearRepository;
    }

    // ============================================================
    // GET BY TERM
    // ============================================================
@Transactional(readOnly = true)
public List<AcademicPeriodDTO> getByTerm(
        UUID schoolId,
        UUID academicYearId,
        UUID academicTermId
) {

    // 1. Vérifier l'année
    yearRepository
            .findByIdAndSchoolId(academicYearId, schoolId)
            .orElseThrow(() -> new EntityNotFoundException("L'année scolaire n'appartient pas à cette école."));

    // 2. Vérifier le trimestre / semestre
    termRepository
            .findByIdAndSchoolIdAndAcademicYearId(academicTermId, schoolId, academicYearId)
            .orElseThrow(() -> new EntityNotFoundException("Le trimestre / semestre n'appartient pas à cette année scolaire."));

    // 3. Récupérer les périodes et mapper vers le DTO
    return periodRepository
            .findBySchoolIdAndAcademicYearIdAndAcademicTermIdOrderByStartDateAsc(
                    schoolId,
                    academicYearId,
                    academicTermId
            )
            .stream()
            .map(period -> AcademicPeriodDTO.builder()
                    .id(period.getId())
                    .schoolId(period.getSchoolId())
                    .academicYearId(period.getAcademicYearId())
                    .academicTermId(period.getAcademicTermId())
                    .name(period.getName())
                    .code(period.getCode())
                    .startDate(period.getStartDate())
                    .endDate(period.getEndDate())
                    .status(period.getStatus())
                    .build())
            .collect(Collectors.toList());
}

    // ============================================================
    // GET BY ACADEMIC YEAR
    // ============================================================

    @Transactional(readOnly = true)
    public List<AcademicPeriodDTO> getByAcademicYear(
            UUID schoolId,
            UUID academicYearId
    ) {

        yearRepository
                .findByIdAndSchoolId(
                        academicYearId,
                        schoolId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Année scolaire introuvable pour cette école."
                        )
                );

        return periodRepository
                .findBySchoolIdAndAcademicYearIdOrderByStartDateAsc(
                        schoolId,
                        academicYearId
                )
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public AcademicPeriodDTO getById(
            UUID schoolId,
            UUID id
    ) {

        AcademicPeriod period = periodRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Période académique introuvable pour cette école."
                        )
                );

        return toDTO(period);
    }

    // ============================================================
    // CREATE
    // ============================================================

    public AcademicPeriodDTO create(
            UUID schoolId,
            AcademicPeriodDTO dto
    ) {

        if (schoolId == null) {
            throw new IllegalArgumentException(
                    "L'école est obligatoire."
            );
        }

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Les données de la période sont obligatoires."
            );
        }

        if (dto.getAcademicYearId() == null) {
            throw new IllegalArgumentException(
                    "L'année scolaire est obligatoire."
            );
        }

        if (dto.getAcademicTermId() == null) {
            throw new IllegalArgumentException(
                    "Le trimestre / semestre est obligatoire."
            );
        }

        // ========================================================
        // 1. VÉRIFIER L'ANNÉE
        // ========================================================

        AcademicYear year = yearRepository
                .findByIdAndSchoolId(
                        dto.getAcademicYearId(),
                        schoolId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "L'année scolaire n'appartient pas à cette école."
                        )
                );

        // ========================================================
        // 2. VÉRIFIER LE TRIMESTRE / SEMESTRE
        // ========================================================

        AcademicTerm term = termRepository
                .findByIdAndSchoolIdAndAcademicYearId(
                        dto.getAcademicTermId(),
                        schoolId,
                        dto.getAcademicYearId()
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Le trimestre / semestre n'appartient pas "
                                + "à cette année scolaire ou à cette école."
                        )
                );

        // ========================================================
        // 3. VALIDATION
        // ========================================================

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de la période est obligatoire."
            );
        }

        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Le code de la période est obligatoire."
            );
        }

        if (dto.getStartDate() == null) {
            throw new IllegalArgumentException(
                    "La date de début est obligatoire."
            );
        }

        if (dto.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "La date de fin est obligatoire."
            );
        }

        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure à la date de début."
            );
        }

        // ========================================================
        // 4. LA PÉRIODE DOIT ÊTRE DANS LE TRIMESTRE / SEMESTRE
        // ========================================================

        if (dto.getStartDate().isBefore(term.getStartDate())
                || dto.getEndDate().isAfter(term.getEndDate())) {

            throw new IllegalArgumentException(
                    "Les dates de la période doivent être comprises "
                    + "dans les dates du trimestre / semestre."
            );
        }

        // ========================================================
        // 5. LA PÉRIODE DOIT ÊTRE DANS L'ANNÉE
        // ========================================================

        if (dto.getStartDate().isBefore(year.getStartDate())
                || dto.getEndDate().isAfter(year.getEndDate())) {

            throw new IllegalArgumentException(
                    "Les dates de la période doivent être comprises "
                    + "dans les dates de l'année scolaire."
            );
        }

        // ========================================================
        // 6. DOUBLON
        // ========================================================

        if (periodRepository
                .existsBySchoolIdAndAcademicTermIdAndCodeIgnoreCase(
                        schoolId,
                        dto.getAcademicTermId(),
                        dto.getCode().trim()
                )) {

            throw new IllegalArgumentException(
                    "Cette période existe déjà dans ce trimestre / semestre."
            );
        }

        // ========================================================
        // 7. CRÉATION
        // ========================================================

        AcademicPeriod period = new AcademicPeriod();

        period.setSchoolId(schoolId);
        period.setAcademicYearId(year.getId());
        period.setAcademicTermId(term.getId());

        period.setName(dto.getName().trim());
        period.setCode(dto.getCode().trim().toUpperCase());

        period.setStartDate(dto.getStartDate());
        period.setEndDate(dto.getEndDate());

        if (dto.getStatus() != null) {
            period.setStatus(dto.getStatus());
        } else {
            period.setStatus(AcademicPeriodStatus.UPCOMING);
        }

        AcademicPeriod saved =
                periodRepository.save(period);

        return toDTO(saved);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public AcademicPeriodDTO update(
            UUID schoolId,
            UUID id,
            AcademicPeriodDTO dto
    ) {

        AcademicPeriod period = periodRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Période académique introuvable."
                        )
                );

        if (dto.getName() != null
                && !dto.getName().isBlank()) {

            period.setName(dto.getName().trim());
        }

        if (dto.getCode() != null
                && !dto.getCode().isBlank()) {

            period.setCode(
                    dto.getCode().trim().toUpperCase()
            );
        }

        if (dto.getStartDate() != null) {
            period.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            period.setEndDate(dto.getEndDate());
        }

        if (period.getStartDate() != null
                && period.getEndDate() != null
                && !period.getEndDate()
                        .isAfter(period.getStartDate())) {

            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure "
                    + "à la date de début."
            );
        }

        if (dto.getStatus() != null) {
            period.setStatus(dto.getStatus());
        }

        return toDTO(
                periodRepository.save(period)
        );
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void delete(
            UUID schoolId,
            UUID id
    ) {

        AcademicPeriod period = periodRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Période académique introuvable."
                        )
                );

        periodRepository.delete(period);
    }

    // ============================================================
    // STATUS
    // ============================================================

    public AcademicPeriodDTO updateStatus(
            UUID schoolId,
            UUID id,
            String status
    ) {

        AcademicPeriod period = periodRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Période académique introuvable."
                        )
                );

        AcademicPeriodStatus newStatus;

        try {

            newStatus = AcademicPeriodStatus
                    .valueOf(status.toUpperCase());

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Statut de période invalide : " + status
            );
        }

        period.setStatus(newStatus);

        return toDTO(
                periodRepository.save(period)
        );
    }

    // ============================================================
    // MAPPING
    // ============================================================

    private AcademicPeriodDTO toDTO(
            AcademicPeriod entity
    ) {

        AcademicPeriodDTO dto =
                new AcademicPeriodDTO();

        dto.setId(entity.getId());

        dto.setSchoolId(entity.getSchoolId());

        dto.setAcademicYearId(
                entity.getAcademicYearId()
        );

        dto.setAcademicTermId(
                entity.getAcademicTermId()
        );

        dto.setName(entity.getName());
        dto.setCode(entity.getCode());

        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());

        dto.setStatus(entity.getStatus());

        return dto;
    }
}
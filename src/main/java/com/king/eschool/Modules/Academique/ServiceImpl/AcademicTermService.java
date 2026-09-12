package com.king.eschool.Modules.Academique.ServiceImpl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicTermDTO;
import com.king.eschool.Modules.Academique.Enum.AcademicTermStatus;
import com.king.eschool.Modules.Academique.Models.AcademicTerm;
import com.king.eschool.Modules.Academique.Models.AcademicYear;
import com.king.eschool.Modules.Academique.Repository.AcademicTermRepository;
import com.king.eschool.Modules.Academique.Repository.AcademicYearRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AcademicTermService {

    private final AcademicTermRepository termRepository;
    private final AcademicYearRepository yearRepository;

    public AcademicTermService(
            AcademicTermRepository termRepository,
            AcademicYearRepository yearRepository
    ) {
        this.termRepository = termRepository;
        this.yearRepository = yearRepository;
    }

    // ============================================================
    // GET BY ACADEMIC YEAR
    // ============================================================

    @Transactional(readOnly = true)
    public List<AcademicTermDTO> getByAcademicYear(
            UUID schoolId,
            UUID academicYearId
    ) {

       List<AcademicTerm> terms = termRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId);
    
return terms.stream()
            .map(term -> AcademicTermDTO.builder()
                    .id(term.getId())
                    .schoolId(term.getSchoolId())           // ✅ Directement getSchoolId()
                    .academicYearId(term.getAcademicYearId()) // ✅ Directement getAcademicYearId()
                    .type(term.getType())
                    .name(term.getName())
                    .code(term.getCode())
                    .startDate(term.getStartDate())
                    .endDate(term.getEndDate())
                    .status(term.getStatus())
                    .build())
            .collect(Collectors.toList());
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public AcademicTermDTO getById(
            UUID schoolId,
            UUID id
    ) {

        AcademicTerm term = termRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Trimestre / semestre introuvable pour cette école."
                        )
                );

        return toDTO(term);
    }

    // ============================================================
    // CREATE
    // ============================================================

    public AcademicTermDTO create(
            UUID schoolId,
            AcademicTermDTO dto
    ) {

        if (schoolId == null) {
            throw new IllegalArgumentException(
                    "L'école est obligatoire."
            );
        }

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Les données du trimestre / semestre sont obligatoires."
            );
        }

        if (dto.getAcademicYearId() == null) {
            throw new IllegalArgumentException(
                    "L'année scolaire est obligatoire."
            );
        }

        // Vérifier que l'année appartient à l'école
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

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du trimestre / semestre est obligatoire."
            );
        }

        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Le code du trimestre / semestre est obligatoire."
            );
        }

        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "Les dates du trimestre / semestre sont obligatoires."
            );
        }

        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure à la date de début."
            );
        }

        // Vérification des dates par rapport à l'année
        if (dto.getStartDate().isBefore(year.getStartDate())
                || dto.getEndDate().isAfter(year.getEndDate())) {

            throw new IllegalArgumentException(
                    "Les dates du trimestre / semestre doivent être comprises "
                    + "dans les dates de l'année scolaire."
            );
        }

        // Éviter les doublons
        if (termRepository.existsBySchoolIdAndAcademicYearIdAndCodeIgnoreCase(
                schoolId,
                dto.getAcademicYearId(),
                dto.getCode().trim()
        )) {

            throw new IllegalArgumentException(
                    "Ce code existe déjà pour cette année scolaire."
            );
        }

        AcademicTerm term = new AcademicTerm();

        term.setSchoolId(schoolId);
        term.setAcademicYearId(year.getId());
        term.setName(dto.getName().trim());
        term.setCode(dto.getCode().trim().toUpperCase());
        term.setStartDate(dto.getStartDate());
        term.setEndDate(dto.getEndDate());

        if (dto.getType() != null) {
            term.setType(dto.getType());
        }

        if (dto.getStatus() != null) {
            term.setStatus(dto.getStatus());
        } else {
            term.setStatus(AcademicTermStatus.UPCOMING);
        }

        AcademicTerm saved = termRepository.save(term);

        return toDTO(saved);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public AcademicTermDTO update(
            UUID schoolId,
            UUID id,
            AcademicTermDTO dto
    ) {

        AcademicTerm term = termRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Trimestre / semestre introuvable."
                        )
                );

        if (dto.getName() != null && !dto.getName().isBlank()) {
            term.setName(dto.getName().trim());
        }

        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            term.setCode(dto.getCode().trim().toUpperCase());
        }

        if (dto.getStartDate() != null) {
            term.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            term.setEndDate(dto.getEndDate());
        }

        if (term.getStartDate() != null
                && term.getEndDate() != null
                && !term.getEndDate().isAfter(term.getStartDate())) {

            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure à la date de début."
            );
        }

        if (dto.getType() != null) {
            term.setType(dto.getType());
        }

        if (dto.getStatus() != null) {
            term.setStatus(dto.getStatus());
        }

        return toDTO(termRepository.save(term));
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void delete(
            UUID schoolId,
            UUID id
    ) {

        AcademicTerm term = termRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Trimestre / semestre introuvable."
                        )
                );

        termRepository.delete(term);
    }

    // ============================================================
    // STATUS
    // ============================================================

    public AcademicTermDTO updateStatus(
            UUID schoolId,
            UUID id,
            String status
    ) {

        AcademicTerm term = termRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Trimestre / semestre introuvable."
                        )
                );

        AcademicTermStatus newStatus;

        try {
            newStatus = AcademicTermStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Statut de trimestre / semestre invalide : " + status
            );
        }

        term.setStatus(newStatus);

        return toDTO(termRepository.save(term));
    }

    // ============================================================
    // MAPPING
    // ============================================================

    private AcademicTermDTO toDTO(AcademicTerm entity) {

        AcademicTermDTO dto = new AcademicTermDTO();

        dto.setId(entity.getId());
        dto.setAcademicYearId(entity.getAcademicYearId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());

        return dto;
    }
}
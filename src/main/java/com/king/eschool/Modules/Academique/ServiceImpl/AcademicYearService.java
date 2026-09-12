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
import java.util.stream.Collectors;

@Service
public class AcademicYearService {
private final AcademicYearRepository academicYearRepository;

    public AcademicYearService(
            AcademicYearRepository academicYearRepository
    ) {
        this.academicYearRepository = academicYearRepository;
    }

    // ============================================================
    // GET ALL
    // ============================================================

    /**
     * Récupérer toutes les années scolaires d'une école.
     */
public List<AcademicYearDTO> getYearsBySchool(UUID schoolId) {
        List<AcademicYear> years = academicYearRepository.findBySchoolId(schoolId);
        
        return years.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AcademicYearDTO mapToDTO(AcademicYear year) {
        // Transformation manuelle ou via MapStruct / ModelMapper
        AcademicYearDTO dto = new AcademicYearDTO();
        dto.setId(year.getId());
        dto.setName(year.getName());
        dto.setStartDate(year.getStartDate());
        dto.setEndDate(year.getEndDate());
        dto.setStatus(year.getStatus());
        return dto;
    }


    // ============================================================
    // GET ACTIVE
    // ============================================================

    /**
     * Récupérer l'année scolaire active d'une école.
     */
    @Transactional(readOnly = true)
    public AcademicYearDTO getActiveYearBySchool(UUID schoolId) {

        AcademicYear year = academicYearRepository
                .findBySchoolIdAndStatus(schoolId, "ACTIVE")
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Aucune année scolaire active pour cette école."
                        )
                );

        return toDTO(year);
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    /**
     * Récupérer une année scolaire.
     *
     * On vérifie également qu'elle appartient bien
     * à l'école fournie.
     */
    @Transactional(readOnly = true)
    public AcademicYearDTO getById(
            UUID schoolId,
            UUID id
    ) {

        AcademicYear year = academicYearRepository
                .findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Année scolaire introuvable pour cette école."
                        )
                );

        return toDTO(year);
    }

    // ============================================================
    // CREATE
    // ============================================================

    /**
     * Créer une année scolaire pour une école.
     */
    public AcademicYearDTO createYear(
            UUID schoolId,
            AcademicYearDTO dto
    ) {

        if (schoolId == null) {
            throw new IllegalArgumentException(
                    "L'école est obligatoire."
            );
        }

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Les données de l'année scolaire sont obligatoires."
            );
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de l'année scolaire est obligatoire."
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

        // Vérifier les doublons dans la même école
        if (academicYearRepository
                .existsBySchoolIdAndNameIgnoreCase(
                        schoolId,
                        dto.getName().trim()
                )) {

            throw new IllegalArgumentException(
                    "Cette année scolaire existe déjà pour cette école."
            );
        }

        AcademicYear year = new AcademicYear();

        year.setSchoolId(schoolId);
        year.setName(dto.getName().trim());
        year.setStartDate(dto.getStartDate());
        year.setEndDate(dto.getEndDate());

        if (dto.getStatus() == null || dto.getStatus().equals("")) {
            year.setStatus(AcademicYearStatus.PREPARATION);
        } else {
            year.setStatus(dto.getStatus());
        }

        AcademicYear saved = academicYearRepository.save(year);

        return toDTO(saved);
    }

    // ============================================================
    // ACTIVATE
    // ============================================================

    /**
     * Active une année scolaire.
     *
     * Important :
     * une seule année ACTIVE par école.
     */
    public AcademicYearDTO activateYear(
            UUID schoolId,
            UUID yearId
    ) {

        AcademicYear year = academicYearRepository
                .findByIdAndSchoolId(yearId, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Année scolaire introuvable pour cette école."
                        )
                );

        // Désactiver l'année actuellement active
        academicYearRepository
                .deactivateAllBySchoolId(schoolId, AcademicYearStatus.CLOSED);

        // Activer celle demandée
        year.setStatus(AcademicYearStatus.ACTIVE);

        AcademicYear saved = academicYearRepository.save(year);

        return toDTO(saved);
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void delete(
            UUID schoolId,
            UUID yearId
    ) {

        AcademicYear year = academicYearRepository
                .findByIdAndSchoolId(yearId, schoolId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Année scolaire introuvable pour cette école."
                        )
                );

        if (AcademicYearStatus.ACTIVE.equals(year.getStatus())) {
            throw new IllegalStateException(
                    "Une année scolaire active ne peut pas être supprimée."
            );
        }

        academicYearRepository.delete(year);
    }

    // ============================================================
    // MAPPING
    // ============================================================

    private AcademicYearDTO toDTO(AcademicYear entity) {

        AcademicYearDTO dto = new AcademicYearDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());

        return dto;
    }

@Transactional
public AcademicYearDTO updateYear(UUID schoolId, UUID yearId, AcademicYearDTO dto) {

    // 1. Récupérer l'entité existante
    AcademicYear year = academicYearRepository.findByIdAndSchoolId(yearId, schoolId)
            .orElseThrow(() -> new EntityNotFoundException("Année académique non trouvée pour cette école."));

    // 2. Mettre à jour les champs
    year.setName(dto.getName());
    year.setStartDate(dto.getStartDate());
    year.setEndDate(dto.getEndDate());
    if (dto.getStatus() != null) {
        year.setStatus(dto.getStatus());
    }

    // 3. Sauvegarder
    AcademicYear updatedYear = academicYearRepository.save(year);

    // 4. Mapper vers DTO (exemple avec Builder)
    return AcademicYearDTO.builder()
            .id(updatedYear.getId())
            .schoolId(updatedYear.getSchoolId())
            .name(updatedYear.getName())
            .startDate(updatedYear.getStartDate())
            .endDate(updatedYear.getEndDate())
            .status(updatedYear.getStatus())
            .build();
}

}

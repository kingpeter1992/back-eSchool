package com.king.eschool.Modules.Academique.ServiceImpl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Audite.Auditable;
import com.king.eschool.Modules.Academique.Dto.Request.AcademicCycleDTO;
import com.king.eschool.Modules.Academique.Dto.Request.AcademicLevelDTO;
import com.king.eschool.Modules.Academique.Dto.Request.AcademicOptionDTO;
import com.king.eschool.Modules.Academique.Dto.Request.AcademicSectionDTO;
import com.king.eschool.Modules.Academique.Dto.Request.CreateOptionDTO;
import com.king.eschool.Modules.Academique.Dto.Request.CreateSectionDTO;
import com.king.eschool.Modules.Academique.Dto.Response.CycleNodeDTO;
import com.king.eschool.Modules.Academique.Dto.Response.LevelNodeDTO;
import com.king.eschool.Modules.Academique.Dto.Response.OptionNodeDTO;
import com.king.eschool.Modules.Academique.Dto.Response.SectionNodeDTO;
import com.king.eschool.Modules.Academique.Models.AcademicCycle;
import com.king.eschool.Modules.Academique.Models.AcademicLevel;
import com.king.eschool.Modules.Academique.Models.AcademicOption;
import com.king.eschool.Modules.Academique.Models.AcademicSection;
import com.king.eschool.Modules.Academique.Repository.AcademicCycleRepository;
import com.king.eschool.Modules.Academique.Repository.AcademicLevelRepository;
import com.king.eschool.Modules.Academique.Repository.AcademicOptionRepository;
import com.king.eschool.Modules.Academique.Repository.AcademicSectionRepository;

import ch.qos.logback.core.util.SystemInfo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademicCycleService {

//Voici la version complète et révisée qui respecte strictement la chaîne de dépendance hiérarchique
//Dépendances et validations appliquéesNiveau : 
// Nécessite un Cycle existant.Section / Option : 
// Nécessite un Cycle (ou un Niveau) existant.Classe :
// La sélection se fait en cascade : 
// Cycle  Niveau - Option (Facultative).Impossible de choisir un Niveau si aucun Cycle n'est sélectionné.Impossible de créer une Classe si aucun Niveau n'a été créé au préalable dans l'établissement.
    private final AcademicCycleRepository cycleRepository;
    private final AcademicLevelRepository levelRepository;
    private final  AcademicSectionRepository sectionRepository;
    private final AcademicOptionRepository optionRepository;

    // UC-AST-001 : Créer un nouveau cycle
@Transactional
@Auditable(action = "CREATE", targetEntity = "ACADEMIC_CYCLE")
public AcademicCycleDTO createCycle(String schoolIdStr, String cycleName) {
    UUID schoolId = UUID.fromString(schoolIdStr);
    AcademicCycle cycle = new AcademicCycle();
    cycle.setSchoolId(schoolId);
    cycle.setName(cycleName);

    return mapCycleToDTO(cycleRepository.save(cycle));
}

    // UC-AST-003 : Configurer un niveau d'études rattaché à un cycle (RG-AST-001 & RG-AST-003)
    public AcademicLevelDTO addLevelToCycle(String cycleIdStr, String levelName, Integer order) {
       
        System.out.print("add level");
        UUID cycleId = UUID.fromString(cycleIdStr);

        AcademicCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new EntityNotFoundException("Cycle académique introuvable"));

        // RG-AST-003 : Vérification de l'unicité du nom au sein de l'école
        if (levelRepository.existsByCycleSchoolIdAndNameIgnoreCase(cycle.getSchoolId(), levelName)) {
            throw new IllegalArgumentException("Un niveau avec le nom '" + levelName + "' existe déjà dans cet établissement.");
        }

        AcademicLevel level = new AcademicLevel();
        level.setCycle(cycle); // RG-AST-001 : Niveau obligatoirement rattaché au cycle parent
        level.setName(levelName);
        level.setNumericOrder(order);

        return mapLevelToDTO(levelRepository.save(level));
    }

    // Récupérer la structure arborescente complète pour une école (F-001)
    @Transactional(readOnly = true)
    public List<AcademicCycleDTO> getFullStructureBySchool(String schoolIdStr) {
        UUID schoolId = UUID.fromString(schoolIdStr);
        return cycleRepository.findBySchoolId(schoolId)
                .stream()
                .map(this::mapCycleToDTO)
                .collect(Collectors.toList());
    }

    // Suppression sécurisée d'un cycle (RG-AST-004)
@Transactional
@Auditable(action = "DELETE", targetEntity = "ACADEMIC_CYCLE")
public void deleteCycle(String cycleIdStr) {
    UUID cycleId = UUID.fromString(cycleIdStr);
    AcademicCycle cycle = cycleRepository.findById(cycleId)
            .orElseThrow(() -> new EntityNotFoundException("Cycle introuvable"));

    boolean hasDependencies = cycle.getLevels().stream()
            .anyMatch(level -> levelRepository.hasActiveClasses(level.getId()));

    if (hasDependencies) {
        throw new IllegalStateException("Impossible de supprimer ce cycle : des classes y sont rattachées.");
    }

    cycleRepository.delete(cycle);
}

    // UC-AST-002 : Ajouter une Section à un Cycle
public AcademicSectionDTO addSectionToCycle(CreateSectionDTO dto) {
    UUID cycleId = UUID.fromString(dto.getCycleId());
    AcademicCycle cycle = cycleRepository.findById(cycleId)
            .orElseThrow(() -> new EntityNotFoundException("Cycle introuvable"));

    AcademicSection section = new AcademicSection();
    section.setCycle(cycle);
    section.setName(dto.getName());

    return mapSectionToDTO(sectionRepository.save(section));
}

// UC-AST-002 : Ajouter une Option à une Section
public AcademicOptionDTO addOptionToSection(CreateOptionDTO dto) {
    UUID sectionId = UUID.fromString(dto.getSectionId());
    AcademicSection section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new EntityNotFoundException("Section introuvable"));

    // RG-AST-003 : Unicité du code d'option dans la même école
    boolean codeExists = optionRepository.existsBySectionCycleSchoolIdAndCodeIgnoreCase(
            section.getCycle().getSchoolId(), dto.getCode()
    );
    if (codeExists) {
        throw new IllegalArgumentException("Le code d'option '" + dto.getCode() + "' existe déjà dans cet établissement.");
    }

    AcademicOption option = new AcademicOption();
    option.setSection(section);
    option.setName(dto.getName());
    option.setCode(dto.getCode().toUpperCase());

    return mapOptionToDTO(optionRepository.save(option));
}

// UC-AST-004 : Associer une option existante à un niveau donné (ex: 3ème Humanités -> Option Math-Physique)
public AcademicLevelDTO assignOptionToLevel(String levelIdStr, String optionIdStr) {
    UUID levelId = UUID.fromString(levelIdStr);
    UUID optionId = UUID.fromString(optionIdStr);

    AcademicLevel level = levelRepository.findById(levelId)
            .orElseThrow(() -> new EntityNotFoundException("Niveau introuvable"));

    AcademicOption option = optionRepository.findById(optionId)
            .orElseThrow(() -> new EntityNotFoundException("Option introuvable"));

    level.setOption(option);
    return mapLevelToDTO(levelRepository.save(level));
}

    // --- MAPPERS UTILS ---

// Mapping complet d'un Cycle (avec ses sections, options et niveaux)
public AcademicCycleDTO mapCycleToDTO(AcademicCycle entity) {
    if (entity == null) return null;

    return AcademicCycleDTO.builder()
            .id(entity.getId() != null ? entity.getId().toString() : null)
            .schoolId(entity.getSchoolId() != null ? entity.getSchoolId().toString() : null)
            .name(entity.getName())
            .sections(entity.getSections() != null 
                    ? entity.getSections().stream().map(this::mapSectionToDTO).toList() 
                    : List.of())
            .levels(entity.getLevels() != null 
                    ? entity.getLevels().stream().map(this::mapLevelToDTO).toList() 
                    : List.of())
            .build();
}


// Mapping d'une Section (avec ses options)
public AcademicSectionDTO mapSectionToDTO(AcademicSection entity) {
    if (entity == null) return null;

    return AcademicSectionDTO.builder()
            .id(entity.getId() != null ? entity.getId().toString() : null)
            .cycleId(entity.getCycle() != null && entity.getCycle().getId() != null 
                    ? entity.getCycle().getId().toString() : null)
            .name(entity.getName())
            .options(entity.getOptions() != null 
                    ? entity.getOptions().stream().map(this::mapOptionToDTO).toList() 
                    : List.of())
            .build();
}

// Mapping d'une Option
public AcademicOptionDTO mapOptionToDTO(AcademicOption entity) {
    if (entity == null) return null;

    return AcademicOptionDTO.builder()
            .id(entity.getId() != null ? entity.getId().toString() : null)
            .sectionId(entity.getSection() != null && entity.getSection().getId() != null 
                    ? entity.getSection().getId().toString() : null)
            .name(entity.getName())
            .code(entity.getCode())
            .build();
}

// Mapping d'un Niveau
public AcademicLevelDTO mapLevelToDTO(AcademicLevel entity) {
    if (entity == null) return null;

    return AcademicLevelDTO.builder()
            .id(entity.getId() != null ? entity.getId().toString() : null)
            .cycleId(entity.getCycle() != null && entity.getCycle().getId() != null 
                    ? entity.getCycle().getId().toString() : null)
            .optionId(entity.getOption() != null && entity.getOption().getId() != null 
                    ? entity.getOption().getId().toString() : null)
            .name(entity.getName())
            .numericOrder(entity.getNumericOrder())
            .build();
    }

@Transactional(readOnly = true)
    public List<AcademicCycleDTO> getCyclesBySchool(String schoolId) {
        // 1. Convertir le String en UUID
        UUID schoolUuid = UUID.fromString(schoolId);

        // 2. Récupérer les entités depuis le repository
        List<AcademicCycle> cycles = cycleRepository.findBySchoolId(schoolUuid);

        // 3. Mapper vers le DTO avec le nom de méthode correct
        return cycles.stream()
                .map(this::mapCycleToDTO)
                .toList();
    }
// ==========================================
    // UPDATES & DELETES - STRUCTURE ACADÉMIQUE
    // ==========================================

    /**
     * Mettre à jour un cycle existant (UC-AST-001)
     */
    public AcademicCycleDTO updateCycle(String id, AcademicCycleDTO dto) {
        UUID cycleId = UUID.fromString(id);
        AcademicCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new EntityNotFoundException("Cycle académique introuvable avec l'ID : " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            cycle.setName(dto.getName());
        }

        return mapCycleToDTO(cycleRepository.save(cycle));
    }

    /**
     * Mettre à jour un niveau d'études (UC-AST-003)
     */
    public AcademicLevelDTO updateLevel(String id, AcademicLevelDTO dto) {
        UUID levelId = UUID.fromString(id);
        AcademicLevel level = levelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Niveau d'études introuvable avec l'ID : " + id));

        // RG-AST-003 : Vérification d'unicité du nom en cas de modification
        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(level.getName())) {
            boolean nameExists = levelRepository.existsByCycleSchoolIdAndNameIgnoreCase(
                    level.getCycle().getSchoolId(), dto.getName());
            if (nameExists) {
                throw new IllegalArgumentException("Un niveau avec le nom '" + dto.getName() + "' existe déjà dans cet établissement.");
            }
            level.setName(dto.getName());
        }

        if (dto.getNumericOrder() != null) {
            level.setNumericOrder(dto.getNumericOrder());
        }

        return mapLevelToDTO(levelRepository.save(level));
    }

    /**
     * Supprimer un niveau d'études
     */
    public void deleteLevel(String id) {
        UUID levelId = UUID.fromString(id);
        AcademicLevel level = levelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Niveau d'études introuvable avec l'ID : " + id));

        // Vérifier si le niveau contient des classes rattachées
        if (levelRepository.hasActiveClasses(levelId)) {
            throw new IllegalStateException("Impossible de supprimer ce niveau : des classes y sont actuellement rattachées.");
        }

        levelRepository.delete(level);
    }

    /**
     * Mettre à jour une section (UC-AST-002)
     */
    public AcademicSectionDTO updateSection(String id, AcademicSectionDTO dto) {
        UUID sectionId = UUID.fromString(id);
        AcademicSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section introuvable avec l'ID : " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            section.setName(dto.getName());
        }

        return mapSectionToDTO(sectionRepository.save(section));
    }

    /**
     * Supprimer une section
     */
    public void deleteSection(String id) {
        UUID sectionId = UUID.fromString(id);
        AcademicSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section introuvable avec l'ID : " + id));

        // Vérifier si la section possède des options rattachées
        if (section.getOptions() != null && !section.getOptions().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer cette section : des options y sont actuellement rattachées.");
        }

        sectionRepository.delete(section);
    }

public void deleteOption(String id) {
    UUID optionId = UUID.fromString(id);
    AcademicOption option = optionRepository.findById(optionId)
            .orElseThrow(() -> new EntityNotFoundException("Option introuvable avec l'ID : " + id));

    optionRepository.delete(option);
}

public List<CycleNodeDTO> getStructureTree(UUID schoolId) {
        // Récupération de tous les cycles de l'école avec leurs relations
        List<AcademicCycle> cycles = cycleRepository.findBySchoolIdWithFullTree(schoolId);

        return cycles.stream().map(cycle -> CycleNodeDTO.builder()
                .id(cycle.getId() != null ? cycle.getId().toString() : null)
                .name(cycle.getName())
                // Construction des niveaux
                .levels(cycle.getLevels() != null
                        ? cycle.getLevels().stream()
                                .sorted(Comparator.comparing(AcademicLevel::getNumericOrder,
                                        Comparator.nullsLast(Integer::compareTo)))
                                .map(level -> LevelNodeDTO.builder()
                                        .id(level.getId() != null ? level.getId().toString() : null)
                                        .name(level.getName())
                                        .numericOrder(level.getNumericOrder())
                                        .build())
                                .collect(Collectors.toList())
                        : List.of())
                // Construction des sections et de leurs options
                .sections(cycle.getSections() != null
                        ? cycle.getSections().stream()
                                .map(section -> SectionNodeDTO.builder()
                                        .id(section.getId() != null ? section.getId().toString() : null)
                                        .name(section.getName())
                                        .options(section.getOptions() != null
                                                ? section.getOptions().stream()
                                                        .map(option -> OptionNodeDTO.builder()
                                                                .id(option.getId() != null ? option.getId().toString() : null)
                                                                .name(option.getName())
                                                                .code(option.getCode())
                                                                .build())
                                                        .collect(Collectors.toList())
                                                : List.of())
                                        .build())
                                .collect(Collectors.toList())
                        : List.of())
                .build())
                .collect(Collectors.toList());
    }

}
package com.king.eschool.Modules.Academique.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicCycleDTO;
import com.king.eschool.Modules.Academique.Dto.Request.AcademicLevelDTO;
import com.king.eschool.Modules.Academique.Dto.Request.AcademicOptionDTO;
import com.king.eschool.Modules.Academique.Dto.Request.AcademicSectionDTO;
import com.king.eschool.Modules.Academique.Dto.Request.CreateCycleRequest;
import com.king.eschool.Modules.Academique.Dto.Request.CreateOptionDTO;
import com.king.eschool.Modules.Academique.Dto.Request.CreateSectionDTO;
import com.king.eschool.Modules.Academique.Dto.Response.CycleNodeDTO;
import com.king.eschool.Modules.Academique.ServiceImpl.AcademicCycleService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-structures")
@RequiredArgsConstructor
public class AcademicStructureController {

    private final AcademicCycleService cycleService;

    /**
     * R (Read) : Récupère toute l'arborescence pédagogique d'une école
     * Permissions : SCHOOL_ADMIN / TEACHER (academic_structure:read.all)
     */
    @GetMapping("/school/{schoolId}")
    @PreAuthorize("hasAuthority('academic_structure:read.all')")
    public ResponseEntity<List<AcademicCycleDTO>> getFullStructure(@PathVariable String schoolId) {
        List<AcademicCycleDTO> structure = cycleService.getFullStructureBySchool(schoolId);
        return ResponseEntity.ok(structure);
    }

    /**
     * C (Create) : Créer un nouveau cycle d'enseignement (UC-AST-001)
     * Permission : SCHOOL_ADMIN (academic_structure:create)
     */
    @PostMapping("/cycles")
    @PreAuthorize("hasAuthority('academic_structure:create')")
    public ResponseEntity<AcademicCycleDTO> createCycle(@Valid @RequestBody CreateCycleRequest dto) {
        AcademicCycleDTO createdCycle = cycleService.createCycle(dto.getSchoolId(), dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCycle);
    }

    /**
     * R (Read) : Récupère la liste des cycles pour un établissement donné
     * Permissions : SCHOOL_ADMIN / TEACHER (academic_structure:read.all)
     */
    @GetMapping("/cycles")
    @PreAuthorize("hasAuthority('academic-year:read.all')")
    public ResponseEntity<List<AcademicCycleDTO>> getCyclesBySchool(@RequestParam String schoolId) {
        List<AcademicCycleDTO> cycles = cycleService.getCyclesBySchool(schoolId);
        return ResponseEntity.ok(cycles);
    }

    /**
     * C (Create) : Ajouter un niveau d'études à un cycle parent (UC-AST-003)
     * Permission : SCHOOL_ADMIN (academic_structure:create)
     */
@PostMapping("/cycles/{cycleId}/levels")
@PreAuthorize("hasAuthority('academic_structure:create')")
public ResponseEntity<AcademicLevelDTO> addLevel(
        @PathVariable String cycleId,
        @RequestBody AcademicLevelDTO dto) { // @RequestBody au lieu de @RequestParam
    
    AcademicLevelDTO createdLevel = cycleService.addLevelToCycle(
        cycleId, dto.getName(), dto.getNumericOrder());
    return ResponseEntity.status(HttpStatus.CREATED).body(createdLevel);
}

    /**
     * D (Delete) : Supprimer un cycle (RG-AST-004 : Bloqué si dépendances)
     * Permission : SCHOOL_ADMIN (academic_structure:delete)
     */
    @DeleteMapping("/cycles/{cycleId}")
    @PreAuthorize("hasAuthority('academic_structure:delete')")
    public ResponseEntity<Void> deleteCycle(@PathVariable String cycleId) {
        cycleService.deleteCycle(cycleId);
        return ResponseEntity.noContent().build();
    }


    /**
 * C (Create) : Ajouter une Section (UC-AST-002)
 */
@PostMapping("/sections")
@PreAuthorize("hasAuthority('academic_structure:create')")
public ResponseEntity<AcademicSectionDTO> createSection(@Valid @RequestBody CreateSectionDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(cycleService.addSectionToCycle(dto));
}

/**
 * C (Create) : Ajouter une Option Pédagogique (UC-AST-002)
 */
@PostMapping("/options")
@PreAuthorize("hasAuthority('academic_structure:create')")
public ResponseEntity<AcademicOptionDTO> createOption(@Valid @RequestBody CreateOptionDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(cycleService.addOptionToSection(dto));
}

/**
 * D (Delete) : Supprimer une option pédagogique
 */
@DeleteMapping("/options/{id}")
@PreAuthorize("hasAuthority('academic_structure:delete')")
public ResponseEntity<Void> deleteOption(@PathVariable String id) {
    cycleService.deleteOption(id);
    return ResponseEntity.noContent().build();
}

/**
 * U (Update) : Lier une Option à un Niveau d'études (UC-AST-004)
 */
@PutMapping("/levels/{levelId}/options/{optionId}")
@PreAuthorize("hasAuthority('academic_structure:update')")
public ResponseEntity<AcademicLevelDTO> assignOptionToLevel(
        @PathVariable String levelId,
        @PathVariable String optionId) {
    return ResponseEntity.ok(cycleService.assignOptionToLevel(levelId, optionId));
}


/**
     * R (Read) : Récupère l'arborescence complète (Cycle -> Niveaux, Sections -> Options)
     * Permissions : SCHOOL_ADMIN / TEACHER (academic_structure:read.all)
     */
    @GetMapping("/tree/{schoolId}")
    @PreAuthorize("hasAuthority('academic_structure:read.all')")
    public ResponseEntity<List<CycleNodeDTO>> getStructureTree(@PathVariable String schoolId) {
        List<CycleNodeDTO> tree = cycleService.getStructureTree(schoolId);
        return ResponseEntity.ok(tree);
    }


// ==========================================
    // UPDATES & DELETES - STRUCTURE ACADÉMIQUE
    // ==========================================

    /**
     * U (Update) : Mettre à jour un cycle (UC-AST-001)
     * Permission : SCHOOL_ADMIN (academic_structure:update)
     */
    @PutMapping("/cycles/{id}")
    @PreAuthorize("hasAuthority('academic_structure:update')")
    public ResponseEntity<AcademicCycleDTO> updateCycle(
            @PathVariable String id,
            @Valid @RequestBody AcademicCycleDTO dto) {
        AcademicCycleDTO updated = cycleService.updateCycle(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * U (Update) : Mettre à jour un niveau d'études (UC-AST-003)
     * Permission : SCHOOL_ADMIN (academic_structure:update)
     */
    @PutMapping("/levels/{id}")
    @PreAuthorize("hasAuthority('academic_structure:update')")
    public ResponseEntity<AcademicLevelDTO> updateLevel(
            @PathVariable String id,
            @Valid @RequestBody AcademicLevelDTO dto) {
        AcademicLevelDTO updated = cycleService.updateLevel(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * D (Delete) : Supprimer un niveau d'études
     * Permission : SCHOOL_ADMIN (academic_structure:delete)
     */
    @DeleteMapping("/levels/{id}")
    @PreAuthorize("hasAuthority('academic_structure:delete')")
    public ResponseEntity<Void> deleteLevel(@PathVariable String id) {
        cycleService.deleteLevel(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * U (Update) : Mettre à jour une section (UC-AST-002)
     * Permission : SCHOOL_ADMIN (academic_structure:update)
     */
    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('academic_structure:update')")
    public ResponseEntity<AcademicSectionDTO> updateSection(
            @PathVariable String id,
            @Valid @RequestBody AcademicSectionDTO dto) {
        AcademicSectionDTO updated = cycleService.updateSection(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * D (Delete) : Supprimer une section
     * Permission : SCHOOL_ADMIN (academic_structure:delete)
     */
    // CORRECT : L'URL finale sera /api/v1/academic-structures/sections/{id}
    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAuthority('academic_structure:delete')")
    public ResponseEntity<Void> deleteSection(@PathVariable String id) {
        cycleService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }


}
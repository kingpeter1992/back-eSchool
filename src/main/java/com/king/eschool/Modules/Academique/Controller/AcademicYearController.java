package com.king.eschool.Modules.Academique.Controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicYearDTO;
import com.king.eschool.Modules.Academique.ServiceImpl.AcademicYearService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic-years")
public class AcademicYearController {
    
    private final AcademicYearService yearService;

    public AcademicYearController(AcademicYearService yearService) {
        this.yearService = yearService;
    }

    // ============================================================
    // ANNÉE SCOLAIRE / ACADÉMIQUE
    // ============================================================

    /**
     * Récupérer toutes les années scolaires d'une école
     */
    @GetMapping("/academic-years-list")
    public ResponseEntity<List<AcademicYearDTO>> getYears(
            @RequestParam UUID schoolId
    ) {
        return ResponseEntity.ok(
                yearService.getYearsBySchool(schoolId)
        );
    }

    /**
     * Récupérer l'année scolaire active d'une école
     */
    @GetMapping("/active")
    public ResponseEntity<AcademicYearDTO> getActiveYear(
            @RequestParam UUID schoolId
    ) {
        return ResponseEntity.ok(
                yearService.getActiveYearBySchool(schoolId)
        );
    }

    /**
     * Créer une année scolaire pour une école
     *
     * Exemple :
     * POST /api/v1/academic-years/academic-years-create?schoolId=xxx
     */
    @PostMapping("/academic-years-create")
    public ResponseEntity<AcademicYearDTO> createYear(
            @RequestParam UUID schoolId,
            @RequestBody AcademicYearDTO dto
    ) {
        return ResponseEntity.ok(
                yearService.createYear(schoolId, dto)
        );
    }

    /**
     * Activer une année scolaire
     *
     * Une seule année devrait être ACTIVE pour une école.
     */
    @PatchMapping("/academic-years/{id}/activate")
    public ResponseEntity<AcademicYearDTO> activateYear(
            @RequestParam UUID schoolId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                yearService.activateYear(schoolId, id)
        );
    }

    /**
     * Récupérer une année scolaire par son ID
     *
     * L'année est également vérifiée par rapport à l'école.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AcademicYearDTO> getYearById(
            @RequestParam UUID schoolId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                yearService.getById(schoolId, id)
        );
    }


    //la  modificatin des l'année scolaire
    @PutMapping("/{id}")
public ResponseEntity<AcademicYearDTO> updateYear(
        @RequestParam UUID schoolId,
        @PathVariable UUID id,
        @RequestBody AcademicYearDTO dto
) {
    return ResponseEntity.ok(
            yearService.updateYear(schoolId, id, dto)
    );
}
    
}
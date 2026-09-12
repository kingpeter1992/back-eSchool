package com.king.eschool.Modules.Academique.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicTermDTO;
import com.king.eschool.Modules.Academique.ServiceImpl.AcademicTermService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic-terms")
@CrossOrigin
public class AcademicTermController {

    private final AcademicTermService termService;

    public AcademicTermController(
            AcademicTermService termService
    ) {
        this.termService = termService;
    }

    // ============================================================
    // GET
    // ============================================================

    /**
     * Récupérer tous les trimestres / semestres
     * d'une année scolaire appartenant à une école.
     *
     * Exemple :
     * GET /api/v1/academic-terms?schoolId=xxx&academicYearId=xxx
     */
    @GetMapping
    public ResponseEntity<List<AcademicTermDTO>> getByAcademicYear(
            @RequestParam UUID schoolId,
            @RequestParam UUID academicYearId
    ) {
        return ResponseEntity.ok(
                termService.getByAcademicYear(
                        schoolId,
                        academicYearId
                )
        );
    }

    /**
     * Récupérer un trimestre / semestre par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AcademicTermDTO> getById(
            @RequestParam UUID schoolId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                termService.getById(schoolId, id)
        );
    }

    // ============================================================
    // CREATE
    // ============================================================

    /**
     * Créer un trimestre ou semestre.
     *
     * Exemple :
     *
     * POST /api/v1/academic-terms
     *
     * {
     *   "academicYearId": "...",
     *   "type": "TRIMESTER",
     *   "name": "Trimestre 1",
     *   "code": "T1",
     *   "startDate": "2026-09-01",
     *   "endDate": "2026-12-20"
     * }
     *
     * L'école est fournie séparément afin de contrôler
     * que l'année scolaire appartient bien à cette école.
     */
    @PostMapping
    public ResponseEntity<AcademicTermDTO> create(
            @RequestParam UUID schoolId,
            @RequestBody AcademicTermDTO dto
    ) {
        return ResponseEntity.ok(
                termService.create(
                        schoolId,
                        dto
                )
        );
    }

    // ============================================================
    // UPDATE
    // ============================================================

    /**
     * Modifier un trimestre / semestre
     */
    @PutMapping("/{id}")
    public ResponseEntity<AcademicTermDTO> update(
            @RequestParam UUID schoolId,
            @PathVariable UUID id,
            @RequestBody AcademicTermDTO dto
    ) {
        return ResponseEntity.ok(
                termService.update(
                        schoolId,
                        id,
                        dto
                )
        );
    }

    // ============================================================
    // DELETE
    // ============================================================

    /**
     * Supprimer un trimestre / semestre
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestParam UUID schoolId,
            @PathVariable UUID id
    ) {
        termService.delete(
                schoolId,
                id
        );

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // STATUS
    // ============================================================

    /**
     * Modifier le statut d'un trimestre / semestre
     *
     * Exemple :
     *
     * PATCH /api/v1/academic-terms/{id}/status
     * ?schoolId=xxx&status=OPEN
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AcademicTermDTO> updateStatus(
            @RequestParam UUID schoolId,
            @PathVariable UUID id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                termService.updateStatus(
                        schoolId,
                        id,
                        status
                )
        );
    } 
}

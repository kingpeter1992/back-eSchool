package com.king.eschool.Modules.Academique.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicPeriodDTO;
import com.king.eschool.Modules.Academique.ServiceImpl.AcademicPeriodService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic-periods")
@CrossOrigin
public class AcademicPeriodController {

    private final AcademicPeriodService periodService;

    public AcademicPeriodController(
            AcademicPeriodService periodService
    ) {
        this.periodService = periodService;
    }

    // ============================================================
    // GET
    // ============================================================

    /**
     * Récupérer les périodes d'un trimestre / semestre.
     *
     * Exemple :
     *
     * GET /api/v1/academic-periods
     * ?schoolId=xxx
     * &academicYearId=xxx
     * &academicTermId=xxx
     */
    @GetMapping
    public ResponseEntity<List<AcademicPeriodDTO>> getByTerm(
            @RequestParam UUID schoolId,
            @RequestParam UUID academicYearId,
            @RequestParam UUID academicTermId
    ) {
        return ResponseEntity.ok(
                periodService.getByTerm(
                        schoolId,
                        academicYearId,
                        academicTermId
                )
        );
    }

    /**
     * Récupérer toutes les périodes d'une année scolaire.
     *
     * Utile pour le calendrier académique.
     */
    @GetMapping("/academic-year/{academicYearId}")
    public ResponseEntity<List<AcademicPeriodDTO>> getByAcademicYear(
            @RequestParam UUID schoolId,
            @PathVariable UUID academicYearId
    ) {
        return ResponseEntity.ok(
                periodService.getByAcademicYear(
                        schoolId,
                        academicYearId
                )
        );
    }

    /**
     * Récupérer une période par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AcademicPeriodDTO> getById(
            @RequestParam UUID schoolId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                periodService.getById(
                        schoolId,
                        id
                )
        );
    }

    // ============================================================
    // CREATE
    // ============================================================

    /**
     * Créer une période dans un trimestre / semestre.
     *
     * Exemple :
     *
     * POST /api/v1/academic-periods
     * ?schoolId=xxx
     *
     * {
     *   "academicYearId": "...",
     *   "academicTermId": "...",
     *   "name": "Période 1",
     *   "code": "P1",
     *   "startDate": "2026-09-01",
     *   "endDate": "2026-10-31",
     *   "status": "UPCOMING"
     * }
     */
    @PostMapping
    public ResponseEntity<AcademicPeriodDTO> create(
            @RequestParam UUID schoolId,
            @RequestBody AcademicPeriodDTO dto
    ) {
        return ResponseEntity.ok(
                periodService.create(
                        schoolId,
                        dto
                )
        );
    }

    // ============================================================
    // UPDATE
    // ============================================================

    /**
     * Modifier une période.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AcademicPeriodDTO> update(
            @RequestParam UUID schoolId,
            @PathVariable UUID id,
            @RequestBody AcademicPeriodDTO dto
    ) {
        return ResponseEntity.ok(
                periodService.update(
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
     * Supprimer une période.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestParam UUID schoolId,
            @PathVariable UUID id
    ) {
        periodService.delete(
                schoolId,
                id
        );

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // STATUS
    // ============================================================

    /**
     * Modifier le statut d'une période.
     *
     * Exemple :
     *
     * PATCH /api/v1/academic-periods/{id}/status
     * ?schoolId=xxx
     * &status=OPEN_FOR_GRADING
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AcademicPeriodDTO> updateStatus(
            @RequestParam UUID schoolId,
            @PathVariable UUID id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                periodService.updateStatus(
                        schoolId,
                        id,
                        status
                )
        );
    } 
}

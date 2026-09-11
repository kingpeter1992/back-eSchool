package com.king.eschool.Modules.Academique.Controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.Academique.Dto.Request.AcademicPeriodDTO;
import com.king.eschool.Modules.Academique.Dto.Request.AcademicYearDTO;
import com.king.eschool.Modules.Academique.ServiceImpl.AcademicPeriodService;
import com.king.eschool.Modules.Academique.ServiceImpl.AcademicYearService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic-years")
public class AcademicYearController {
    
    private final AcademicYearService yearService;
    private final AcademicPeriodService periodService;

    public AcademicYearController(AcademicYearService yearService, AcademicPeriodService periodService) {
        this.yearService = yearService;
        this.periodService = periodService;
    }

    // ➕ AJOUT : Endpoint pour récupérer l'année académique active
   @GetMapping("/active")
public ResponseEntity<AcademicYearDTO> getActiveYear(@RequestParam UUID schoolId) {
    return ResponseEntity.ok(yearService.getActiveYearBySchool(schoolId));
}
        // --- Années Scolaires ---
    @PostMapping("/academic-years-create")
    public ResponseEntity<AcademicYearDTO> createYear(
        @RequestParam UUID schoolId, @RequestBody AcademicYearDTO dto) {
        return ResponseEntity.ok(yearService.createYear(schoolId, dto));
    }

    @GetMapping("/academic-years-list")
    public ResponseEntity<List<AcademicYearDTO>> getYears(@RequestParam UUID schoolId) {
        return ResponseEntity.ok(yearService.getYearsBySchool(schoolId));
    }

    @PatchMapping("/academic-years/{id}/activate")
    public ResponseEntity<AcademicYearDTO> activateYear(
        @RequestParam UUID schoolId, @PathVariable UUID id) {
        return ResponseEntity.ok(yearService.activateYear(schoolId, id));
    }

    // --- Périodes Académiques ---
    @PostMapping("/academic-periods")
    public ResponseEntity<AcademicPeriodDTO> createPeriod(@RequestBody AcademicPeriodDTO dto) {
        return ResponseEntity.ok(periodService.createPeriod(dto));
    }

    @GetMapping("/academic-periods")
    public ResponseEntity<List<AcademicPeriodDTO>> getPeriods(@RequestParam String yearId) {
        return ResponseEntity.ok(periodService.getPeriodsByYear(yearId));
    }
}
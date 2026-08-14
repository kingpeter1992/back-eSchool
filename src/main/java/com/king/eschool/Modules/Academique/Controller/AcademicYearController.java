package com.king.eschool.Modules.Academique.Controller;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.Academique.Models.AcademicYear;
import com.king.eschool.Modules.Academique.ServiceImpl.AcademicYearService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('academic:create')")
    public ResponseEntity<AcademicYear> create(@Valid @RequestBody AcademicYear year) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicYearService.createAcademicYear(year));
    }

    @GetMapping("/school/{schoolId}")
    @PreAuthorize("hasAuthority('academic:read')")
    public ResponseEntity<List<AcademicYear>> getBySchool(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(academicYearService.getYearsBySchool(schoolId));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('academic:update')")
    public ResponseEntity<Void> activate(@PathVariable UUID id, @RequestParam UUID schoolId) {
        academicYearService.activateYear(id, schoolId);
        return ResponseEntity.noContent().build();
    }
}
package com.king.eschool.Modules.School.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.School.Dto.reponse.CampusResponseDto;
import com.king.eschool.Modules.School.Dto.request.CampusRequestDto;
import com.king.eschool.Modules.School.ServiceImplement.SchoolServiceImpl;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final SchoolServiceImpl campusService;

    // UC-CAM-001 : Créer un campus
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:create'))")
    public ResponseEntity<CampusResponseDto> createCampus(@Valid @RequestBody CampusRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campusService.createCampus(request));
    }

    // UC-CAM-003 : Lister les campus d'une école
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAnyAuthority('campus:read.all', 'campus:read'))")
    public ResponseEntity<List<CampusResponseDto>> getCampusesBySchool(@RequestParam UUID schoolId) {
        return ResponseEntity.ok(campusService.getCampusesBySchool(schoolId));
    }

    // Consulter un campus par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:read'))")
    public ResponseEntity<CampusResponseDto> getCampusById(@PathVariable UUID id) {
        return ResponseEntity.ok(campusService.getCampusById(id));
    }

    // Soft-delete d'un campus
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:delete'))")
    public ResponseEntity<Void> deleteCampus(@PathVariable UUID id) {
        campusService.deleteCampus(id);
        return ResponseEntity.noContent().build();
    }
}
package com.king.eschool.Modules.Admission.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.Modules.Admission.Dto.request.AssignClassRequestDTO;
import com.king.eschool.Modules.Admission.Dto.request.CreateEnrollmentRequestDTO;
import com.king.eschool.Modules.Admission.Dto.request.UpdateEnrollmentStatusRequestDTO;
import com.king.eschool.Modules.Admission.Dto.response.ClassCapacityDto;
import com.king.eschool.Modules.Admission.Dto.response.EnrollmentResponseDTO;
import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;
import com.king.eschool.Modules.Admission.ServiceImpl.EnrollmentServiceImpl;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentServiceImpl enrollmentService;
    
   // Expression réutilisable d'administration globale
    private static final String ADMIN_ROLES = "hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN_ECOLE') or hasAuthority('ROLE_ADMIN')";

   // ============================================================
    // CREATE
    // ============================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('enrollment:create')")
    public ResponseEntity<EnrollmentResponseDTO> create(
            @RequestPart("data") @Valid CreateEnrollmentRequestDTO request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.create(request, photo, documents));
    }

    // ============================================================
    // READ / SEARCH
    // ============================================================
    @GetMapping
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:read')")
    public ResponseEntity<List<EnrollmentResponseDTO>> getAll() {
        return ResponseEntity.ok(enrollmentService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:read')")
    public ResponseEntity<EnrollmentResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.getById(id));
    }

    @GetMapping("/school/{schoolId}")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:read')")
    public ResponseEntity<List<EnrollmentResponseDTO>> getBySchool(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(enrollmentService.getBySchool(schoolId));
    }

    @GetMapping("/academic-year/{academicYearId}")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:read')")
    public ResponseEntity<List<EnrollmentResponseDTO>> getByAcademicYear(@PathVariable UUID academicYearId) {
        return ResponseEntity.ok(enrollmentService.getByAcademicYear(academicYearId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:read')")
    public ResponseEntity<List<EnrollmentResponseDTO>> getByStatus(@PathVariable EnrollmentStatus status) {
        return ResponseEntity.ok(enrollmentService.getByStatus(status));
    }

    @GetMapping("/available-classes")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:read')")
    public ResponseEntity<List<ClassCapacityDto>> getAvailableClasses(
            @RequestParam UUID campusId,
            @RequestParam UUID levelId,
            @RequestParam UUID academicYearId) {
        return ResponseEntity.ok(enrollmentService.getAvailableClassesWithCapacity(campusId, levelId, academicYearId));
    }

    // ============================================================
    // UPDATE & ACTIONS METIER
    // ============================================================
    @PutMapping("/{id}/status")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:update')")
    public ResponseEntity<EnrollmentResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEnrollmentStatusRequestDTO request) {
        return ResponseEntity.ok(enrollmentService.updateStatus(id, request));
    }

    @PutMapping("/{id}/assign-class")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:assign_class')")
    public ResponseEntity<EnrollmentResponseDTO> assignClass(
            @PathVariable UUID id,
            @Valid @RequestBody AssignClassRequestDTO dto) {
        dto.setEnrollmentId(id);
        return ResponseEntity.ok(enrollmentService.assignClass(dto));
    }

    // ============================================================
    // DELETE
    // ============================================================
    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
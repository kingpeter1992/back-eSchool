package com.king.eschool.Modules.Admission.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.Audite.ServiceImpl.AuditService;
import com.king.eschool.Audite.models.AuditEvent;
import com.king.eschool.Modules.Admission.Dto.request.AssignClassRequestDTO;
import com.king.eschool.Modules.Admission.Dto.request.CreateEnrollmentRequestDTO;
import com.king.eschool.Modules.Admission.Dto.request.UpdateEnrollmentStatusRequestDTO;
import com.king.eschool.Modules.Admission.Dto.response.ClassCapacityDto;
import com.king.eschool.Modules.Admission.Dto.response.EnrollmentResponseDTO;
import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;
import com.king.eschool.Modules.Admission.ServiceImpl.EnrollmentServiceImpl;

// Import de votre service d'audit et DTO Event

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentServiceImpl enrollmentService;
    private final AuditService auditService;

    private static final String ADMIN_ROLES = "hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN_ECOLE') or hasAuthority('ROLE_ADMIN')";

    // ============================================================
    // UTILS AUDIT
    // ============================================================
    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getCurrentUsername() {
        Authentication auth = getAuth();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";
    }

    private String getCurrentUserRoles() {
        Authentication auth = getAuth();
        if (auth == null || !auth.isAuthenticated()) return "NONE";
        return auth.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    // ============================================================
    // CREATE
    // ============================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('enrollment:create')")
    public ResponseEntity<EnrollmentResponseDTO> create(
            @RequestPart("data") @Valid CreateEnrollmentRequestDTO request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            HttpServletRequest httpRequest
    ) {
        EnrollmentResponseDTO response = enrollmentService.create(request, photo, documents);

        // Appel de l'audit après création
        auditService.logEvent(AuditEvent.builder()
                .username(getCurrentUsername())
                .userRole(getCurrentUserRoles())
                .schoolId(request.getSchoolId())
                .campusId(request.getCampusId())
                .ipAddress(getClientIp(httpRequest))
                .deviceInfo(httpRequest.getHeader("User-Agent"))
                .actionType("CREATE_ENROLLMENT")
                .targetEntity("Enrollment")
                .targetId(response.getId().toString())
                .newValue(response)
                .details("Création de dossier d'inscription pour : " + request.getCandidateFirstName() + " " + request.getCandidateLastName())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            @Valid @RequestBody UpdateEnrollmentStatusRequestDTO request,
            HttpServletRequest httpRequest) {

        EnrollmentResponseDTO oldState = enrollmentService.getById(id);
        EnrollmentResponseDTO newState = enrollmentService.updateStatus(id, request);

        auditService.logEvent(AuditEvent.builder()
                .username(getCurrentUsername())
                .userRole(getCurrentUserRoles())
                .schoolId(newState.getSchoolId())
                .campusId(newState.getCampusId())
                .ipAddress(getClientIp(httpRequest))
                .deviceInfo(httpRequest.getHeader("User-Agent"))
                .actionType("UPDATE_ENROLLMENT_STATUS")
                .targetEntity("Enrollment")
                .targetId(id.toString())
                .oldValue(oldState.getStatus())
                .newValue(newState.getStatus())
                .details("Mise à jour du statut vers : " + request.getStatus() + (request.getRejectionReason() != null ? " - Motif: " + request.getRejectionReason() : ""))
                .build());

        return ResponseEntity.ok(newState);
    }

    @PutMapping("/{id}/assign-class")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:assign_class')")
    public ResponseEntity<EnrollmentResponseDTO> assignClass(
            @PathVariable UUID id,
            @Valid @RequestBody AssignClassRequestDTO dto,
            HttpServletRequest httpRequest) {

        dto.setEnrollmentId(id);
        EnrollmentResponseDTO oldState = enrollmentService.getById(id);
        EnrollmentResponseDTO newState = enrollmentService.assignClass(dto);

        auditService.logEvent(AuditEvent.builder()
                .username(getCurrentUsername())
                .userRole(getCurrentUserRoles())
                .schoolId(newState.getSchoolId())
                .campusId(newState.getCampusId())
                .ipAddress(getClientIp(httpRequest))
                .deviceInfo(httpRequest.getHeader("User-Agent"))
                .actionType("ASSIGN_CLASS")
                .targetEntity("Enrollment")
                .targetId(id.toString())
                .oldValue(oldState != null ? oldState.getClassId() : null)
                .newValue(newState != null ? newState.getClassId() : null)
                .details("Affectation de la classe ID : " + dto.getClassId())
                .build());

        return ResponseEntity.ok(newState);
    }

    // ============================================================
    // DELETE
    // ============================================================
    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES + " or hasAuthority('enrollment:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest httpRequest) {

        EnrollmentResponseDTO oldState = enrollmentService.getById(id);

        enrollmentService.delete(id);

        auditService.logEvent(AuditEvent.builder()
                .username(getCurrentUsername())
                .userRole(getCurrentUserRoles())
                .schoolId(oldState != null ? oldState.getSchoolId() : null)
                .campusId(oldState != null ? oldState.getCampusId() : null)
                .ipAddress(getClientIp(httpRequest))
                .deviceInfo(httpRequest.getHeader("User-Agent"))
                .actionType("DELETE_ENROLLMENT")
                .targetEntity("Enrollment")
                .targetId(id.toString())
                .oldValue(oldState)
                .details("Suppression définitive du dossier N° " + (oldState != null ? oldState.getRegistrationNo() : id))
                .build());

        return ResponseEntity.noContent().build();
    }
}
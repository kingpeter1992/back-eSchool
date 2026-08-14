package com.king.eschool.Modules.School.Controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.School.Dto.reponse.SchoolResponseDto;
import com.king.eschool.Modules.School.Dto.request.SchoolRequestDto;
import com.king.eschool.Modules.School.ServiceImplement.SchoolServiceImpl;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools")
public class SchoolController {

    private final SchoolServiceImpl schoolService;

    public SchoolController(SchoolServiceImpl schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
   @PreAuthorize("hasAuthority('school:read.all')")
    public ResponseEntity<List<SchoolResponseDto>> getAllSchools() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('school:read')")
    public ResponseEntity<SchoolResponseDto> getSchoolById(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolService.getSchoolById(id));
    }

    @PostMapping
  @PreAuthorize("hasAuthority('school:create')")
    public ResponseEntity<SchoolResponseDto> createSchool(@Valid @RequestBody SchoolRequestDto requestDto) {
        return ResponseEntity.ok(schoolService.createSchool(requestDto));
    }

    @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('school:update')")
    public ResponseEntity<SchoolResponseDto> updateSchool(@PathVariable UUID id, @Valid @RequestBody SchoolRequestDto requestDto) {
        return ResponseEntity.ok(schoolService.updateSchool(id, requestDto));
    }

    @PatchMapping("/{id}/status")
  @PreAuthorize("hasAuthority('school:status.update')")
    public ResponseEntity<SchoolResponseDto> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        return ResponseEntity.ok(schoolService.updateSchoolStatus(id, status));
    }

    @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('school:delete')")
    public ResponseEntity<Void> deleteSchool(@PathVariable UUID id) {
        schoolService.softDeleteSchool(id);
        return ResponseEntity.noContent().build();
    }
}
package com.king.eschool.Modules.Academique.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.Academique.Dto.Request.CreateClassDTO;
import com.king.eschool.Modules.Academique.Dto.Request.UpdateClassDTO;
import com.king.eschool.Modules.Academique.Dto.Response.SchoolClassResponseDTO;
import com.king.eschool.Modules.Academique.ServiceImpl.ClassServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class ClassController {

   private final ClassServiceImpl classService;

    @PostMapping
    @PreAuthorize("hasAuthority('class:create')")
    public ResponseEntity<SchoolClassResponseDTO> createClass(@Valid @RequestBody CreateClassDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.createClass(dto));
    }

    @GetMapping("/campus/{campusId}")
    @PreAuthorize("hasAuthority('class:read.all')")
    public ResponseEntity<List<SchoolClassResponseDTO>> getClassesByCampus(@PathVariable String campusId) {
        return ResponseEntity.ok(classService.getClassesByCampus(campusId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('class:read')")
    public ResponseEntity<SchoolClassResponseDTO> getClassById(@PathVariable String id) {
        return ResponseEntity.ok(classService.getClassById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('class:update')")
    public ResponseEntity<SchoolClassResponseDTO> updateClass(
            @PathVariable String id,
            @Valid @RequestBody UpdateClassDTO dto) {
        return ResponseEntity.ok(classService.updateClass(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('class:delete')")
    public ResponseEntity<Void> deleteClass(@PathVariable String id) {
        classService.softDeleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
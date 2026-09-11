package com.king.eschool.Modules.Admission.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.king.eschool.Modules.Admission.Dto.response.EnrollmentResponseDTO;
import com.king.eschool.Modules.Admission.Dto.response.EnrollmentStatusResponseDTO;
import com.king.eschool.Modules.Admission.ServiceImpl.EnrollmentServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/enrollments/public")
@RequiredArgsConstructor
public class PublicEnrollmentController {

    private final EnrollmentServiceImpl enrollmentService;

    @GetMapping("/status/{registrationNo}")
    public ResponseEntity<EnrollmentStatusResponseDTO> getStatus(@PathVariable String registrationNo) {
        return ResponseEntity.ok(enrollmentService.getPublicStatus(registrationNo));
    }
}
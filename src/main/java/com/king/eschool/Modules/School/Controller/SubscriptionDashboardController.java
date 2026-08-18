    package com.king.eschool.Modules.School.Controller;


import com.king.eschool.Modules.School.Dto.reponse.SubscriptionDashboardDto;
import com.king.eschool.Modules.School.Dto.request.SendEmailRequestDto;
import com.king.eschool.Modules.School.Dto.request.SubscriptionItemDto;
import com.king.eschool.Modules.School.Dto.request.SubscriptionRequestDto;
import com.king.eschool.Modules.School.ServiceImplement.SubscriptionDashboardService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionDashboardController {

    private final SubscriptionDashboardService dashboardService;

    public SubscriptionDashboardController(SubscriptionDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // GET /api/v1/subscriptions/dashboard
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('school:read.all') or hasAuthority('subscription:read')")
    public ResponseEntity<SubscriptionDashboardDto> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    // GET /api/v1/subscriptions/dashboard/school/{schoolId}
    @GetMapping("/dashboard/school/{schoolId}")
    public ResponseEntity<SubscriptionItemDto> getDashboardBySchool(@PathVariable String schoolId) {
        return ResponseEntity.ok(dashboardService.getDashboardBySchoolId(schoolId));
    }

    // GET /api/v1/subscriptions
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('school:read.all')")
    public ResponseEntity<List<SubscriptionItemDto>> getAllSubscriptions() {
        return ResponseEntity.ok(dashboardService.getAllSubscriptions());
    }

    // POST /api/v1/subscriptions/school/{schoolId}
    @PostMapping("/school/{schoolId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('subscription:write')")
    public ResponseEntity<SubscriptionItemDto> createSubscription(
            @PathVariable UUID schoolId, 
            @Valid @RequestBody SubscriptionRequestDto request) {
        return ResponseEntity.ok(dashboardService.createAndActivate(schoolId, request));
    }

    // POST /api/v1/subscriptions/school/{schoolId}/send-email
    @PostMapping("/school/{schoolId}/send-email")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('subscription:write')")
    public ResponseEntity<Void> sendEmailToSchool(
            @PathVariable UUID schoolId, 
            @Valid @RequestBody SendEmailRequestDto request) {
        dashboardService.sendManualEmail(schoolId, request.getSubject(), request.getMessage());
        return ResponseEntity.noContent().build();
    }
}
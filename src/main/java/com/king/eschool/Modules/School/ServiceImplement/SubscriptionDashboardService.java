package com.king.eschool.Modules.School.ServiceImplement;


import com.king.eschool.Core.config.EmailServiceImpl;
import com.king.eschool.Modules.School.Dto.SchoolStatus;
import com.king.eschool.Modules.School.Dto.reponse.SubscriptionDashboardDto;
import com.king.eschool.Modules.School.Dto.request.SubscriptionItemDto;
import com.king.eschool.Modules.School.Dto.request.SubscriptionRequestDto;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Models.Subscription;
import com.king.eschool.Modules.School.Models.Subscription.SubscriptionStatus;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.Modules.School.Repository.SubscriptionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionDashboardService {

    private final SchoolRepository schoolRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailServiceImpl emailService;

    @Transactional(readOnly = true)
    public SubscriptionDashboardDto getDashboardStats() {
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        long total = allSubscriptions.size();
        long activeCount = allSubscriptions.stream().filter(s -> s.getStatus() == Subscription.SubscriptionStatus.ACTIVE).count();
        long trialCount = allSubscriptions.stream().filter(s -> s.getStatus() == Subscription.SubscriptionStatus.TRIAL).count();
        long expiredCount = allSubscriptions.stream().filter(s -> s.getStatus() == Subscription.SubscriptionStatus.EXPIRED).count();
        long suspendedCount = allSubscriptions.stream().filter(s -> s.getStatus() == Subscription.SubscriptionStatus.SUSPENDED).count();

        BigDecimal mrr = allSubscriptions.stream()
                .filter(s -> s.getStatus() == Subscription.SubscriptionStatus.ACTIVE && s.getAmount() != null)
                .map(Subscription::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> countByPlan = allSubscriptions.stream()
                .collect(Collectors.groupingBy(s -> s.getPlanType().name(), Collectors.counting()));

        return SubscriptionDashboardDto.builder()
                .monthlyRecurringRevenue(mrr)
                .annualRecurringRevenue(mrr.multiply(BigDecimal.valueOf(12)))
                .totalSubscriptions(total)
                .activeSubscriptions(activeCount)
                .trialSubscriptions(trialCount)
                .expiredSubscriptions(expiredCount)
                .suspendedSubscriptions(suspendedCount)
                .countByPlanType(countByPlan)
                .build();
    }

    @Transactional
    public SubscriptionItemDto createAndActivate(UUID schoolId, SubscriptionRequestDto request) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(schoolId)
                .orElseThrow(() -> new EntityNotFoundException("Établissement introuvable."));

        school.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE)
                .forEach(sub -> sub.setStatus(Subscription.SubscriptionStatus.EXPIRED));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMonths(request.getDurationInMonths());

        Subscription subscription = Subscription.builder()
                .school(school)
                .planType(request.getPlanType())
                .startsAt(now)
                .expiresAt(expiresAt)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build();

        school.addSubscription(subscription);
        school.setStatus(SchoolStatus.ACTIVE);
        schoolRepository.save(school);

        String formattedDate = expiresAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        emailService.sendSubscriptionActivationEmail(school.getEmail(), school.getName(), subscription.getPlanType().name(), formattedDate);

        return toDto(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionItemDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void sendManualEmail(UUID schoolId, String subject, String message) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(schoolId)
                .orElseThrow(() -> new EntityNotFoundException("Établissement introuvable."));

        emailService.sendCustomSchoolEmail(school.getEmail(), subject, message);
    }

    private SubscriptionItemDto toDto(Subscription sub) {
        boolean expired = sub.getExpiresAt().isBefore(LocalDateTime.now()) || sub.getStatus() == Subscription.SubscriptionStatus.EXPIRED;
        return SubscriptionItemDto.builder()
                .id(sub.getId())
                .schoolId(sub.getSchool().getId())
                .schoolName(sub.getSchool().getName())
                .schoolEmail(sub.getSchool().getEmail())
                .planType(sub.getPlanType())
                .startsAt(sub.getStartsAt())
                .expiresAt(sub.getExpiresAt())
                .amount(sub.getAmount())
                .currency(sub.getCurrency())
                .status(sub.getStatus())
                .isExpired(expired)
                .build();
    }

@Transactional(readOnly = true)
public SubscriptionItemDto getDashboardBySchoolId(String schoolId) {
    UUID uuid = parseSchoolUuid(schoolId);

    // 1. Vérification si l'école existe
    if (!schoolRepository.existsByIdAndDeletedAtIsNull(uuid)) {
        throw new EntityNotFoundException("Établissement introuvable avec l'ID : " + schoolId);
    }

    // 2. Recherche directe du dernier abonnement via le repository
    Subscription latestSubscription = subscriptionRepository.findFirstBySchoolIdOrderByExpiresAtDesc(uuid)
            .orElseThrow(() -> new EntityNotFoundException("Aucun abonnement trouvé pour cet établissement."));

    // 3. Conversion en DTO
    return toDto(latestSubscription);
}

// Méthode utilitaire pour convertir proprement le String en UUID
private UUID parseSchoolUuid(String schoolId) {
    try {
        return UUID.fromString(schoolId);
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Le format de l'identifiant de l'école est invalide : " + schoolId);
    }
}
}
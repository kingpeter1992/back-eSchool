package com.king.eschool.Modules.School.Dto.reponse;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class SubscriptionDashboardDto {
    // KPI Financiers
    private BigDecimal monthlyRecurringRevenue; // MRR
    private BigDecimal annualRecurringRevenue;  // ARR
    private BigDecimal totalRevenueCollected;

    // KPI Volumétrie / Statuts
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long trialSubscriptions;
    private long expiredSubscriptions;
    private long suspendedSubscriptions;

    // Répartition des plans (ex: {"PREMIUM": 12, "BASIC": 45})
    private Map<String, Long> countByPlanType;

    // Taux de conversion
    private double conversionRate; // % d'écoles passées de TRIAL à ACTIVE
}

package com.king.eschool.Modules.School.Dto.request;


import com.king.eschool.Modules.School.Models.Subscription.PlanType;
import com.king.eschool.Modules.School.Models.Subscription.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SubscriptionItemDto {
    private UUID id;
    private UUID schoolId;
    private String schoolName;
    private String schoolEmail;
    private PlanType planType;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private BigDecimal amount;
    private String currency;
    private SubscriptionStatus status;
    private boolean isExpired;
}
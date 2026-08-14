package com.king.eschool.Modules.School.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.School.Models.Subscription;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findTopBySchoolIdOrderByExpiresAtDesc(UUID schoolId);
}
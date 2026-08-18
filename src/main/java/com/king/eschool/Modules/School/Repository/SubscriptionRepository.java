package com.king.eschool.Modules.School.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.School.Models.Subscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findTopBySchoolIdOrderByExpiresAtDesc(UUID schoolId);
    Optional<Subscription> findFirstBySchoolIdOrderByExpiresAtDesc(UUID schoolId);

    // 🟢 Recherche les abonnements actifs dont l'expiration correspond exactement à une plage de date
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.expiresAt >= :startOfDay AND s.expiresAt <= :endOfDay")
    List<Subscription> findExpiringSubscriptionsBetween(
            @Param("startOfDay") LocalDateTime startOfDay, 
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
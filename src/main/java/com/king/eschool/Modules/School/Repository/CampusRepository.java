package com.king.eschool.Modules.School.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.School.Models.Campus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampusRepository extends JpaRepository<Campus, UUID> {
    List<Campus> findBySchoolIdAndDeletedAtIsNull(UUID schoolId);

    // Ne récupère que les campus non supprimés (soft-delete)
    @Query("SELECT c FROM Campus c WHERE c.school.id = :schoolId AND c.deletedAt IS NULL")
    List<Campus> findAllBySchoolIdAndNotDeleted(UUID schoolId);

    @Query("SELECT c FROM Campus c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Campus> findByIdAndNotDeleted(UUID id);

    // Vérifie l'unicité du code au sein de la même école (RG-CAM-002)
    boolean existsBySchoolIdAndDeletedAtIsNull(UUID schoolId);

}
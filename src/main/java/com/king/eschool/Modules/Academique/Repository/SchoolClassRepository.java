package com.king.eschool.Modules.Academique.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Models.SchoolClass;
import com.king.eschool.Modules.Academique.Models.ShiftType;

import jakarta.persistence.LockModeType;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
List<SchoolClass> findByCampusIdAndDeletedFalse(UUID campusId);

    List<SchoolClass> findBySchoolIdAndDeletedFalse(UUID schoolId);

    // Unicité du nom par campus
    boolean existsByCampusIdAndNameIgnoreCaseAndDeletedFalse(UUID campusId, String name);

    @Query(value = "SELECT 0", nativeQuery = true)
    long countActiveStudentsInClass(@Param("classId") UUID classId);

    // Compte toutes les classes non supprimées associées à une salle donnée
    long countByRoomIdAndDeletedFalse(UUID roomId);

    // Compte les classes sur une salle en excluant la classe en cours d'édition
    long countByRoomIdAndIdNotAndDeletedFalse(UUID roomId, UUID currentClassId);

    // Vérifie si une salle est déjà occupée sur un créneau donné (Création)
boolean existsByRoomIdAndShiftAndDeletedFalse(UUID roomId, ShiftType shift);

// Vérifie la disponibilité de la salle/créneau lors de la modification (Exclut la classe modifiée)
boolean existsByRoomIdAndShiftAndIdNotAndDeletedFalse(UUID roomId, ShiftType shift, UUID classId);

List<SchoolClass> findByCampusIdAndLevelId(UUID campusId, UUID levelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM SchoolClass c WHERE c.id = :id")
    Optional<SchoolClass> findByIdForUpdate(@Param("id") UUID id);

}
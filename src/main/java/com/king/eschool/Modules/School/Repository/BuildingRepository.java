package com.king.eschool.Modules.School.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.king.eschool.Modules.School.Models.Building;
public interface BuildingRepository extends JpaRepository<Building, UUID>{

    boolean existsByCodeAndCampusId(String code, UUID campusId);

// ✅ CORRECTION : Retourner l'entité Building
    List<Building> findByCampusId(UUID campusId);

  @Query("SELECT DISTINCT b FROM Building b " +
           "LEFT JOIN FETCH b.rooms " +
           "WHERE b.campus.id = :campusId")
    List<Building> findByCampusIdWithRooms(@Param("campusId") UUID campusId);

  @Query("SELECT DISTINCT b FROM Building b " +
       "LEFT JOIN FETCH b.rooms r " +
       "LEFT JOIN FETCH r.equipments " +
       "WHERE b.campus.id = :campusId")
List<Building> findByCampusIdWithRoomsAndEquipments(@Param("campusId") UUID campusId);
}

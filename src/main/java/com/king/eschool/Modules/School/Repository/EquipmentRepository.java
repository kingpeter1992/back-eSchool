package com.king.eschool.Modules.School.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.School.Models.Equipment;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
}
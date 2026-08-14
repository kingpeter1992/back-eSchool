package com.king.eschool.Modules.School.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.School.Models.Campus;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampusRepository extends JpaRepository<Campus, UUID> {
    List<Campus> findBySchoolIdAndDeletedAtIsNull(UUID schoolId);
}
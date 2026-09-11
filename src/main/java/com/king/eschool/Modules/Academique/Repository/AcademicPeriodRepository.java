package com.king.eschool.Modules.Academique.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.Academique.Models.AcademicPeriod;

import java.util.List;
import java.util.UUID;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, String> {
List<AcademicPeriod> findByAcademicYearId(UUID academicYearId);
}
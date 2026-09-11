package com.king.eschool.Modules.School.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.School.Models.CampusSchedule;

public interface CampusScheduleRepository extends JpaRepository<CampusSchedule, String> {
    List<CampusSchedule> findByCampusId(String campusId);
    Optional<CampusSchedule> findByCampusIdAndDayOfWeek(String campusId, DayOfWeek dayOfWeek);
    
}

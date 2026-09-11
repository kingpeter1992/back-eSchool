package com.king.eschool.Modules.School.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
@Entity
@Table(name = "campus_schedules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"campus_id", "day_of_week"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "campus_id", nullable = false)
    private String campusId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "is_open", nullable = false)
    private boolean isOpen;

    // --- Vacation Matin ---
    @Column(name = "morning_start_time")
    private LocalTime morningStartTime;

    @Column(name = "morning_end_time")
    private LocalTime morningEndTime;

    // --- Vacation Soir ---
    @Column(name = "evening_start_time")
    private LocalTime eveningStartTime;

    @Column(name = "evening_end_time")
    private LocalTime eveningEndTime;
}
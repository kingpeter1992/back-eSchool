package com.king.eschool.Modules.School.ServiceImplement;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Modules.School.Dto.request.ScheduleDTO;
import com.king.eschool.Modules.School.Models.CampusSchedule;
import com.king.eschool.Modules.School.Repository.CampusScheduleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampusScheduleService {

    private final CampusScheduleRepository scheduleRepository;
public List<ScheduleDTO> getSchedulesByCampus(String campusId) {
        return scheduleRepository.findByCampusId(campusId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ScheduleDTO> saveOrUpdateSchedules(String campusId, List<ScheduleDTO> dtoList) {
        // Validation des heures directement sur l'élément 'dto' (ScheduleDTO)
        for (ScheduleDTO dto : dtoList) {
            if (dto.isOpen()) {
                // Validation Vacation Matin
                if (dto.morningStartTime() != null && dto.morningEndTime() != null) {
                    if (dto.morningStartTime().isAfter(dto.morningEndTime())) {
                        throw new IllegalArgumentException("L'heure de début de matinée est postérieure à l'heure de fin pour " + dto.dayOfWeek());
                    }
                }
                // Validation Vacation Soir
                if (dto.eveningStartTime() != null && dto.eveningEndTime() != null) {
                    if (dto.eveningStartTime().isAfter(dto.eveningEndTime())) {
                        throw new IllegalArgumentException("L'heure de début de soirée est postérieure à l'heure de fin pour " + dto.dayOfWeek());
                    }
                }
            }
        }

        List<CampusSchedule> entities = dtoList.stream().map(dto -> {
            CampusSchedule schedule = scheduleRepository
                    .findByCampusIdAndDayOfWeek(campusId, dto.dayOfWeek())
                    .orElse(new CampusSchedule());

            schedule.setCampusId(campusId);
            schedule.setDayOfWeek(dto.dayOfWeek());
            schedule.setOpen(dto.isOpen());

            if (dto.isOpen()) {
                schedule.setMorningStartTime(dto.morningStartTime());
                schedule.setMorningEndTime(dto.morningEndTime());
                schedule.setEveningStartTime(dto.eveningStartTime());
                schedule.setEveningEndTime(dto.eveningEndTime());
            } else {
                schedule.setMorningStartTime(null);
                schedule.setMorningEndTime(null);
                schedule.setEveningStartTime(null);
                schedule.setEveningEndTime(null);
            }

            return schedule;
        }).collect(Collectors.toList());

        List<CampusSchedule> saved = scheduleRepository.saveAll(entities);
        return saved.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ScheduleDTO mapToDTO(CampusSchedule entity) {
        return new ScheduleDTO(
                entity.getId(),
                entity.getDayOfWeek(),
                entity.isOpen(),
                entity.getMorningStartTime(),
                entity.getMorningEndTime(),
                entity.getEveningStartTime(),
                entity.getEveningEndTime()
        );
    }

}
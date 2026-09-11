package com.king.eschool.Modules.Academique.ServiceImpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Audite.ServiceImpl.AuditService;
import com.king.eschool.Audite.models.AuditEvent;
import com.king.eschool.Modules.Academique.Dto.Request.CreateClassDTO;
import com.king.eschool.Modules.Academique.Dto.Request.UpdateClassDTO;
import com.king.eschool.Modules.Academique.Dto.Response.SchoolClassResponseDTO;
import com.king.eschool.Modules.Academique.Enum.ClassStatus;
import com.king.eschool.Modules.Academique.Models.AcademicLevel;
import com.king.eschool.Modules.Academique.Models.SchoolClass;
import com.king.eschool.Modules.Academique.Models.ShiftType;
import com.king.eschool.Modules.Academique.Repository.AcademicLevelRepository;
import com.king.eschool.Modules.Academique.Repository.SchoolClassRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassServiceImpl {

    private static final String TARGET_ENTITY = "SchoolClass";

    private final SchoolClassRepository classRepository;
    private final AcademicLevelRepository levelRepository;
    private final AuditService auditService;

    @Transactional
    public SchoolClassResponseDTO createClass(CreateClassDTO dto) {
        UUID schoolId = UUID.fromString(dto.getSchoolId());
        UUID campusId = UUID.fromString(dto.getCampusId());
        UUID levelId = UUID.fromString(dto.getLevelId());

        if (classRepository.existsByCampusIdAndNameIgnoreCaseAndDeletedFalse(campusId, dto.getName())) {
            throw new IllegalArgumentException("Une classe nommée '" + dto.getName() + "' existe déjà sur ce campus.");
        }

        UUID roomId = (dto.getRoomId() != null && !dto.getRoomId().isBlank()) ? UUID.fromString(dto.getRoomId()) : null;

        if (roomId != null && dto.getShift() != null) {
            validateRoomShift(roomId, dto.getShift(), null);
        }

        AcademicLevel level = levelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Niveau d'études introuvable."));

        SchoolClass schoolClass = SchoolClass.builder()
                .schoolId(schoolId)
                .campusId(campusId)
                .level(level)
                .name(dto.getName())
                .maxCapacity(dto.getMaxCapacity() != null ? dto.getMaxCapacity() : 40)
                .status(ClassStatus.OPEN)
                .roomId(roomId)
                .shift(dto.getShift())
                .build();

        if (dto.getMainTeacherId() != null && !dto.getMainTeacherId().isBlank()) {
            schoolClass.setMainTeacherId(UUID.fromString(dto.getMainTeacherId()));
        }

        SchoolClass savedClass = classRepository.save(schoolClass);
        SchoolClassResponseDTO response = mapToDTO(savedClass, 0);

        auditService.logEvent(AuditEvent.builder()
                .schoolId(schoolId)
                .campusId(campusId)
                .actionType("CLASS_CREATED")
                .targetEntity(TARGET_ENTITY)
                .targetId(savedClass.getId().toString())
                .oldValue(null)
                .newValue(response)
                .details("Création de la classe '" + savedClass.getName() + "'")
                .build());

        return response;
    }

    @Transactional
    public SchoolClassResponseDTO updateClass(String classIdStr, UpdateClassDTO dto) {
        UUID classId = UUID.fromString(classIdStr);
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Classe introuvable."));

        SchoolClassResponseDTO oldState = mapToDTO(schoolClass, classRepository.countActiveStudentsInClass(classId));

        UUID targetRoomId = (dto.getRoomId() != null && !dto.getRoomId().isBlank())
                ? UUID.fromString(dto.getRoomId())
                : schoolClass.getRoomId();

        ShiftType targetShift = dto.getShift() != null ? dto.getShift() : schoolClass.getShift();

        if (targetRoomId != null && targetShift != null) {
            if (!targetRoomId.equals(schoolClass.getRoomId()) || targetShift != schoolClass.getShift()) {
                validateRoomShift(targetRoomId, targetShift, classId);
            }
        }

        schoolClass.setRoomId(targetRoomId);
        schoolClass.setShift(targetShift);

        if (dto.getMaxCapacity() != null) {
            schoolClass.setMaxCapacity(dto.getMaxCapacity());
        }
        if (dto.getName() != null) {
            schoolClass.setName(dto.getName());
        }

        SchoolClass updatedClass = classRepository.save(schoolClass);
        SchoolClassResponseDTO newState = mapToDTO(updatedClass, classRepository.countActiveStudentsInClass(classId));

        auditService.logEvent(AuditEvent.builder()
                .schoolId(schoolClass.getSchoolId())
                .campusId(schoolClass.getCampusId())
                .actionType("CLASS_UPDATED")
                .targetEntity(TARGET_ENTITY)
                .targetId(updatedClass.getId().toString())
                .oldValue(oldState)
                .newValue(newState)
                .details("Mise à jour de la classe '" + updatedClass.getName() + "'")
                .build());

        return newState;
    }

    @Transactional
    public void softDeleteClass(String classId) {
        UUID id = UUID.fromString(classId);
        SchoolClass schoolClass = classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Classe introuvable."));

        if (classRepository.countActiveStudentsInClass(id) > 0) {
            throw new IllegalStateException("Impossible de supprimer une classe contenant des élèves.");
        }

        SchoolClassResponseDTO oldState = mapToDTO(schoolClass, 0);

        schoolClass.setDeleted(true);
        schoolClass.setStatus(ClassStatus.CLOSED);
        classRepository.save(schoolClass);

        auditService.logEvent(AuditEvent.builder()
                .schoolId(schoolClass.getSchoolId())
                .campusId(schoolClass.getCampusId())
                .actionType("CLASS_DELETED")
                .targetEntity(TARGET_ENTITY)
                .targetId(schoolClass.getId().toString())
                .oldValue(oldState)
                .newValue(null)
                .details("Suppression logique (soft delete) de la classe '" + schoolClass.getName() + "'")
                .build());
    }

    @Transactional(readOnly = true)
    public SchoolClassResponseDTO getClassById(String classIdStr) {
        UUID classId = UUID.fromString(classIdStr);

        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Classe introuvable avec l'ID : " + classIdStr));

        long currentEnrollment = classRepository.countActiveStudentsInClass(classId);

        return mapToDTO(schoolClass, currentEnrollment);
    }

    @Transactional(readOnly = true)
    public List<SchoolClassResponseDTO> getClassesByCampus(String campusIdStr) {
        UUID campusId = UUID.fromString(campusIdStr);

        return classRepository.findByCampusIdAndDeletedFalse(campusId)
                .stream()
                .map(cls -> mapToDTO(cls, classRepository.countActiveStudentsInClass(cls.getId())))
                .collect(Collectors.toList());
    }

    private void validateRoomShift(UUID roomId, ShiftType shift, UUID currentClassId) {
        boolean isOccupied;
        if (currentClassId != null) {
            isOccupied = classRepository.existsByRoomIdAndShiftAndIdNotAndDeletedFalse(roomId, shift, currentClassId);
        } else {
            isOccupied = classRepository.existsByRoomIdAndShiftAndDeletedFalse(roomId, shift);
        }

        if (isOccupied) {
            String shiftLabel = (shift == ShiftType.MORNING) ? "Avant-midi" : "Après-midi";
            throw new IllegalStateException("Cette salle est déjà occupée pour le créneau du " + shiftLabel + ".");
        }
    }

    public SchoolClassResponseDTO mapToDTO(SchoolClass entity, long currentEnrollment) {
        if (entity == null) {
            return null;
        }

        return SchoolClassResponseDTO.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .schoolId(entity.getSchoolId() != null ? entity.getSchoolId().toString() : null)
                .campusId(entity.getCampusId() != null ? entity.getCampusId().toString() : null)
                .levelId(entity.getLevel() != null ? entity.getLevel().getId().toString() : null)
                .levelName(entity.getLevel() != null ? entity.getLevel().getName() : null)
                .name(entity.getName())
                .roomId(entity.getRoomId() != null ? entity.getRoomId().toString() : null)
                .shift(entity.getShift())
                .maxCapacity(entity.getMaxCapacity())
                .currentEnrollment(currentEnrollment)
                .mainTeacherId(entity.getMainTeacherId() != null ? entity.getMainTeacherId().toString() : null)
                .status(entity.getStatus())
                .build();
    }
}
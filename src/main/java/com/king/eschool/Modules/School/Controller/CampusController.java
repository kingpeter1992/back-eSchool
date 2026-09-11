package com.king.eschool.Modules.School.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.king.eschool.Modules.School.Dto.reponse.BuildingResponseDto;
import com.king.eschool.Modules.School.Dto.reponse.CampusResponseDto;
import com.king.eschool.Modules.School.Dto.reponse.EquipmentResponseDto;
import com.king.eschool.Modules.School.Dto.reponse.RoomResponseDto;
import com.king.eschool.Modules.School.Dto.request.AssignManagerDto;
import com.king.eschool.Modules.School.Dto.request.BuildingRequestDto;
import com.king.eschool.Modules.School.Dto.request.CampusCapacityDto;
import com.king.eschool.Modules.School.Dto.request.CampusRequestDto;
import com.king.eschool.Modules.School.Dto.request.CampusScheduleRequest;
import com.king.eschool.Modules.School.Dto.request.ChangeCampusStatusDto;
import com.king.eschool.Modules.School.Dto.request.EquipmentRequestDto;
import com.king.eschool.Modules.School.Dto.request.RoomRequestDto;
import com.king.eschool.Modules.School.Dto.request.ScheduleDTO;
import com.king.eschool.Modules.School.Models.Building;
import com.king.eschool.Modules.School.Models.Room;
import com.king.eschool.Modules.School.ServiceImplement.CampusScheduleService;
import com.king.eschool.Modules.School.ServiceImplement.SchoolServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final SchoolServiceImpl campusService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:create'))")
    public ResponseEntity<CampusResponseDto> createCampus(
            @Valid @RequestBody CampusRequestDto request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campusService.createCampus(request));
    }

    // UC-CAM-003 : Lister les campus d'une école
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAnyAuthority('campus:read.all', 'campus:read'))")
    public ResponseEntity<List<CampusResponseDto>> getCampusesBySchool(@RequestParam UUID schoolId) {
        return ResponseEntity.ok(campusService.getCampusesBySchool(schoolId));
    }

    // Consulter un campus par ID
     @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:read.all'))")
    public ResponseEntity<CampusResponseDto> getCampusById(@PathVariable UUID id) {
        return ResponseEntity.ok(campusService.getCampusById(id));
    }

    // Soft-delete d'un campus
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:delete'))")
    public ResponseEntity<Void> deleteCampus(@PathVariable UUID id) {
        campusService.deleteCampus(id);
        return ResponseEntity.noContent().build();
    }

    // UC-CAM-004 : Modifier un campus existant
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<CampusResponseDto> updateCampus(
            @PathVariable UUID id,
            @Valid @RequestBody CampusRequestDto request) {
        return ResponseEntity.ok(campusService.updateCampus(id, request));
    }

    // UC-CAM-011 & UC-CAM-004 : Changement de statut (ACTIVE, SUSPENDED, ARCHIVED)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<CampusResponseDto> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeCampusStatusDto request) {
        return ResponseEntity.ok(campusService.updateCampusStatus(id, request.getStatus()));
    }

    // UC-CAM-005 : Affecter un responsable au campus
    @PatchMapping("/{id}/manager")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<CampusResponseDto> assignManager(
            @PathVariable UUID id,
            @Valid @RequestBody AssignManagerDto request) {
        return ResponseEntity.ok(campusService.assignManager(id, request.getManagerId()));
    }

    // UC-CAM-008 : Déclarer la capacité
    @PatchMapping("/{id}/capacity")
    //@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<CampusResponseDto> updateCapacity(
            @PathVariable UUID id,
            @Valid @RequestBody CampusCapacityDto request) {
        return ResponseEntity.ok(campusService.updateCapacity(id, request.getTotalCapacity()));
    }

    // UC-CAM-006 : Ajouter un bâtiment
    @PostMapping("/{id}/buildings")
    //@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<BuildingResponseDto> addBuilding(
            @PathVariable UUID id,
            @Valid @RequestBody BuildingRequestDto request) {

        // Appels du service métier
        Building building = campusService.addingBuild(id, request);

        // Mapping vers DTO pour éviter toute boucle infinie Jackson
        BuildingResponseDto response = BuildingResponseDto.builder()
                .id(building.getId())
                .name(building.getName())
                .code(building.getCode())
                .campusId(id)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UC-CAM-006 : Modifier un bâtiment existant
    @PutMapping("/buildings/{buildingId}")
    //@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<BuildingResponseDto> updateBuilding(
            @PathVariable UUID buildingId,
            @Valid @RequestBody BuildingRequestDto request) {

        // Appel du service métier pour la mise à jour
        Building building = campusService.updateBuilding(buildingId, request);

        // Mappage vers le DTO de réponse
        BuildingResponseDto response = BuildingResponseDto.builder()
                .id(building.getId())
                .name(building.getName())
                .code(building.getCode())
                .campusId(building.getCampus().getId()) // Assurez-vous d'avoir accès au campus
                .build();

        return ResponseEntity.ok(response);
    }

    // UC-CAM-006 : Lister les bâtiments d'un campus et ses détails
@GetMapping("/{id}/buildings")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or " +
              "hasAuthority('ROLE_ADMIN') or " +
              "(hasAuthority('ROLE_ADMIN_ECOLE') and hasAnyAuthority('campus:read.all', 'campus:read'))")
    public ResponseEntity<List<BuildingResponseDto>> getBuildingsByCampus(@PathVariable UUID id) {
        return ResponseEntity.ok(campusService.getBuildingsByCampus(id));
    }

    // UC-CAM-007 & UC-CAM-009 : Ajouter une salle avec équipements à un bâtiment
    @PostMapping("/buildings/{buildingId}/rooms")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
     @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<RoomResponseDto> addRoom(
            @PathVariable UUID buildingId,
            @Valid @RequestBody RoomRequestDto request) {

        Room room = campusService.addRoomToBuilding(buildingId, request);

        // Mappage de Set<Equipment> vers List<EquipmentResponseDto>
        List<EquipmentResponseDto> equipmentDtos = room.getEquipments() != null
                ? room.getEquipments().stream()
                        .map(eq -> EquipmentResponseDto.builder()
                                .id(eq.getId())
                                .name(eq.getName())
                                .quantity(eq.getQuantity())
                                .state(eq.getState())
                                .build())
                        .toList()
                : Collections.emptyList();

        RoomResponseDto response = RoomResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .equipments(equipmentDtos) // 👈 Compatible avec List<EquipmentResponseDto>
                .buildingId(buildingId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UC-CAM-008 : Modifier les informations d'une salle et ses équipements
    @PutMapping("/rooms/{roomId}")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
     @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")    
    public ResponseEntity<RoomResponseDto> updateRoom(
            @PathVariable UUID roomId,
            @Valid @RequestBody RoomRequestDto request) {

        RoomResponseDto response = campusService.updateRoom(roomId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rooms/{roomId}/equipments/{equipmentId}")
      @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<RoomResponseDto> updateEquipment(
            @PathVariable UUID roomId,
            @PathVariable UUID equipmentId,
            @Valid @RequestBody EquipmentRequestDto request) {

        RoomResponseDto updatedRoom = campusService.updateEquipment(roomId, equipmentId, request);
        return ResponseEntity.ok(updatedRoom);
    }

    @PostMapping("/rooms/{roomId}/equipments") // 👈 Route exacte demandée
         @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<RoomResponseDto> addEquipment(
            @PathVariable UUID roomId,
            @Valid @RequestBody EquipmentRequestDto dto) {

        RoomResponseDto updatedRoom = campusService.addEquipmentToRoom(roomId, dto);

        return ResponseEntity.ok(updatedRoom);
    }

    // Supprimer un équipement d'une salle
    @DeleteMapping("/rooms/{roomId}/equipments/{equipmentId}")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
     @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")

    public ResponseEntity<Void> deleteEquipment(
            @PathVariable UUID roomId,
            @PathVariable UUID equipmentId) {

        campusService.deleteEquipmentFromRoom(roomId, equipmentId);
        return ResponseEntity.noContent().build();
    }

    private final CampusScheduleService scheduleService;

    /**
     * Récupère la liste des horaires et vacations (Matin / Soir) pour un campus.
     */
    @GetMapping("/{campusId}/schedules")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
     @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<List<ScheduleDTO>> getSchedules(@PathVariable String campusId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesByCampus(campusId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Met à jour les horaires et vacations d'un campus.
     */
    @PutMapping("/{campusId}/schedules")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
         @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_ADMIN_ECOLE') and hasAuthority('campus:update'))")
    public ResponseEntity<List<ScheduleDTO>> saveSchedules(
            @PathVariable String campusId,
            @RequestBody CampusScheduleRequest request) {

        List<ScheduleDTO> updatedSchedules = scheduleService.saveOrUpdateSchedules(campusId, request.schedules());
        return ResponseEntity.ok(updatedSchedules);
    }
}
package com.king.eschool.Modules.School.ServiceImplement;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.king.eschool.Audite.Auditable;
import com.king.eschool.Modules.Cours.Repository.CourseRepository;
import com.king.eschool.Modules.Parent.repository.ParentRepository;
import com.king.eschool.Modules.School.Dto.SchoolStatus;
import com.king.eschool.Modules.School.Dto.reponse.BuildingResponseDto;
import com.king.eschool.Modules.School.Dto.reponse.CampusResponseDto;
import com.king.eschool.Modules.School.Dto.reponse.EquipmentResponseDto;
import com.king.eschool.Modules.School.Dto.reponse.RoomResponseDto;
import com.king.eschool.Modules.School.Dto.reponse.SchoolResponseDto;
import com.king.eschool.Modules.School.Dto.request.BuildingRequestDto;
import com.king.eschool.Modules.School.Dto.request.CampusRequestDto;
import com.king.eschool.Modules.School.Dto.request.EquipmentRequestDto;
import com.king.eschool.Modules.School.Dto.request.RoomRequestDto;
import com.king.eschool.Modules.School.Dto.request.SchoolRequestDto;
import com.king.eschool.Modules.School.Interfaces.ISchoolService;
import com.king.eschool.Modules.School.Models.Building;
import com.king.eschool.Modules.School.Models.Campus;
import com.king.eschool.Modules.School.Models.Campus.CampusStatus;
import com.king.eschool.Modules.School.Models.Equipment;
import com.king.eschool.Modules.School.Models.Room;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.BuildingRepository;
import com.king.eschool.Modules.School.Repository.CampusRepository;
import com.king.eschool.Modules.School.Repository.EquipmentRepository;
import com.king.eschool.Modules.School.Repository.RoomRepository;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.Modules.Student.repository.StudentRepository;
import com.king.eschool.Modules.Teach.TeacherRepository;
import com.king.eschool.shared.Storage.Services.FileStorageService;
import com.king.eschool.shared.Storage.dtoResponse.FileDocumentResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements ISchoolService {

    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final FileStorageService fileStorageService;
    private final BuildingRepository buildingRepository;
    private final RoomRepository  roomRepository;
    private final EquipmentRepository equipmentRepository;
    private  final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final CourseRepository courseRepository;


    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponseDto> getAllSchools() {
        return schoolRepository.findAll().stream()
                .filter(s -> s.getDeletedAt() == null)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponseDto> getAllSchools(SchoolStatus status) {
        if (status == null) {
            return getAllSchools();
        }
        return schoolRepository.findByStatusAndDeletedAtIsNull(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolResponseDto getSchoolById(UUID id) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable ou supprimé."));
        return toDto(school);
    }

    @Override
    @Transactional
    @Auditable(action = "CREATE", targetEntity = "SCHOOL")
    public SchoolResponseDto createSchool(SchoolRequestDto requestDto) {
        if (requestDto.getDomain() != null && !requestDto.getDomain().isBlank()
                && schoolRepository.existsByDomain(requestDto.getDomain())) {
            throw new IllegalArgumentException("Ce sous-domaine est déjà utilisé sur la plateforme.");
        }

        String generatedCode = generateUniqueSchoolCode();

        School school = School.builder()
                .name(requestDto.getName())
                .code(generatedCode)
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .currency(requestDto.getCurrency() != null ? requestDto.getCurrency() : "USD")
                .timezone(requestDto.getTimezone() != null ? requestDto.getTimezone() : "UTC")
                .domain(requestDto.getDomain())
                .status(SchoolStatus.PENDING)
                .build();

        // 1. Sauvegarde initiale pour obtenir l'ID de l'école
        School savedSchool = schoolRepository.save(school);

        // 2. Si un fichier logo est fourni à la création, on l'uploade
        if (requestDto.getLogo() != null && !requestDto.getLogo().isEmpty()) {
            String logoUrl = processLogoUpload(savedSchool.getId(), requestDto.getLogo());
            savedSchool.setLogoUrl(logoUrl);
            savedSchool = schoolRepository.save(savedSchool);
        }

        return toDto(savedSchool);
    }

    @Override
@Transactional
@Auditable(action = "UPDATE", targetEntity = "SCHOOL")
public SchoolResponseDto updateSchool(UUID id, SchoolRequestDto requestDto) {
    School school = schoolRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Établissement introuvable.")); // Utiliser une exception dédiée

    school.setName(requestDto.getName());
    school.setEmail(requestDto.getEmail());
    school.setPhone(requestDto.getPhone());
    
    if (requestDto.getCurrency() != null) school.setCurrency(requestDto.getCurrency());
    if (requestDto.getTimezone() != null) school.setTimezone(requestDto.getTimezone());
    if (requestDto.getDomain() != null) school.setDomain(requestDto.getDomain());

    // 🟢 Traitement du logo uniquement si un nouveau fichier valide est fourni
    if (requestDto.getLogo() != null && !requestDto.getLogo().isEmpty()) {
        
        // Supprimer l'ancien logo si présent
        if (school.getLogoUrl() != null && !school.getLogoUrl().isBlank()) {
            try {
                fileStorageService.deleteFileByUrl(school.getLogoUrl());
            } catch (Exception e) {
                new RuntimeException("Impossible de supprimer l'ancien logo Supabase: {}" +e.getMessage());
            }
        }
        
        String newLogoUrl = processLogoUpload(school.getId(), requestDto.getLogo());
        school.setLogoUrl(newLogoUrl);
    }

    School updated = schoolRepository.save(school);
    return toDto(updated);
}

    @Override
    @Transactional
    @Auditable(action = "UPDATE_STATUS", targetEntity = "SCHOOL")
    public SchoolResponseDto updateSchoolStatus(UUID id, String statusStr) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable."));

        SchoolStatus newStatus = SchoolStatus.valueOf(statusStr.toUpperCase());
        school.setStatus(newStatus);

        School updated = schoolRepository.save(school);
        return toDto(updated);
    }

    @Override
    @Transactional
    @Auditable(action = "UPLOAD_LOGO", targetEntity = "SCHOOL")
    public SchoolResponseDto uploadLogo(UUID schoolId, MultipartFile file) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(schoolId)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable."));

        if (school.getLogoUrl() != null && !school.getLogoUrl().isBlank()) {
            try {
                fileStorageService.deleteFileByUrl(school.getLogoUrl());
            } catch (Exception ignored) {
            }
        }

        String logoUrl = processLogoUpload(schoolId, file);
        school.setLogoUrl(logoUrl);

        School updated = schoolRepository.save(school);
        return toDto(updated);
    }

    
    @Override
    @Transactional
    @Auditable(action = "SOFT_DELETE", targetEntity = "SCHOOL")
    public void softDeleteSchool(UUID id) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable."));

        school.setDeletedAt(LocalDateTime.now());
        school.setStatus(SchoolStatus.DELETED);
        schoolRepository.save(school);
    }

    // Méthode utilitaire interne pour l'envoi de fichier vers Supabase
    private String processLogoUpload(UUID schoolId, MultipartFile file) {
        Long refId = Math.abs(schoolId.getMostSignificantBits());
        FileDocumentResponse response = fileStorageService.uploadFile(file, "SCHOOL_LOGOS", schoolId);
        return response.getPublicUrl();
    }

    private String generateUniqueSchoolCode() {
        String code;
        do {
            int randomNum = (int) (Math.random() * 90000) + 10000;
            code = "SCH" + LocalDateTime.now().getYear() + randomNum;
        } while (schoolRepository.existsByCode(code));
        return code;
    }



    @Transactional
    public CampusResponseDto createCampus(CampusRequestDto dto) {
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("École introuvable"));

        // RG-CAM-002 : Unicité du code par école
        if (campusRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Le code campus '" + dto.getCode() + "' existe déjà pour cette école.");
        } 

        Campus campus = Campus.builder()
                .school(school)
                .name(dto.getName())
                .code(dto.getCode())
                .address(dto.getAddress())
                .city(dto.getCity())
                .country(dto.getCountry())
                .phone(dto.getPhone())
                .status(Campus.CampusStatus.ACTIVE)
                .build();

        return mapToDto(campusRepository.save(campus));
    }

    public List<CampusResponseDto> getCampusesBySchool(UUID schoolId) {
        return campusRepository.findAllBySchoolIdAndNotDeleted(schoolId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CampusResponseDto getCampusById(UUID id) {
        Campus campus = campusRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Campus introuvable"));
        return mapToDto(campus);
    }

    @Transactional
    public void deleteCampus(UUID id) {
        Campus campus = campusRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Campus introuvable"));

        // RG-CAM-003 : Soft-delete via deleted_at
        campus.setDeletedAt(LocalDateTime.now());
        campus.setStatus(Campus.CampusStatus.INACTIVE);
        campusRepository.save(campus);
    }

//     private CampusResponseDto mapToDto(Campus campus) {
//         return CampusResponseDto.builder()
//                 .id(campus.getId())
//                 .schoolId(campus.getSchool().getId())
//                 .schoolName(campus.getSchool().getName())
//                 .name(campus.getName())
// //                .code(campus.getCode())
//                 .address(campus.getAddress())
//                 .city(campus.getCity())
//                 .country(campus.getCountry())
//                 .phone(campus.getPhone())
//                 .status(campus.getStatus())
//                 .createdAt(campus.getCreatedAt())
//                 .build();
//     }


private SchoolResponseDto toDto(School school) {
    // 1. Mapping et filtrage des campus actifs (Soft-delete)
    List<CampusResponseDto> campusList = school.getCampuses() != null
            ? school.getCampuses().stream()
                    .filter(c -> c.getDeletedAt() == null)
                    .map(c -> CampusResponseDto.builder()
                            .id(c.getId())
                            .schoolId(school.getId())
                            .schoolName(school.getName())
                            .name(c.getName())
                            .address(c.getAddress())
                            .city(c.getCity())
                            .country(c.getCountry())
                            .phone(c.getPhone())
                            .status(c.getStatus())
                            .createdAt(c.getCreatedAt())
                            .build())
                    .collect(Collectors.toList())
            : Collections.emptyList();

    SchoolResponseDto dto = new SchoolResponseDto();
    
    // 2. Informations de base de l'école
    dto.setId(school.getId());
    dto.setName(school.getName());
    dto.setCode(school.getCode());
    dto.setEmail(school.getEmail());
    dto.setPhone(school.getPhone());
    dto.setLogoUrl(school.getLogoUrl());
    dto.setCurrency(school.getCurrency());
    dto.setTimezone(school.getTimezone());
    dto.setDomain(school.getDomain());
    dto.setStatus(school.getStatus());
    dto.setCampuses(campusList);
    dto.setCreatedAt(school.getCreatedAt());

    // 3. Calcul dynamique des statistiques
    dto.setTotalCampuses((long) campusList.size());
    
    // Remplacer par vos appels de repositories/services respectifs (ou 0L par défaut)

//    dto.setTotalStudents(studentRepository.countBySchoolId(school.getId()));
//    dto.setTotalTeachers(teacherRepository.countBySchoolId(school.getId()));
//    dto.setTotalClasses(schoolClassRepository.countBySchoolId(school.getId()));
//    dto.setTotalCourses(courseRepository.countBySchoolId(school.getId()));
 //   dto.setTotalParents(parentRepository.countBySchoolId(school.getId()));
    dto.setTotalStudents(0L);
    dto.setTotalTeachers(0L);
    dto.setTotalClasses(0L);
    dto.setTotalCourses(0L);
    dto.setTotalParents(0L);

    return dto;
}

@Override
public CampusResponseDto addCampus(UUID schoolId, CampusRequestDto request) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addCampus'");
}

@Transactional
    @Auditable(action = "UPDATE", targetEntity = "CAMPUS")
    public CampusResponseDto updateCampus(UUID id, CampusRequestDto dto) {
        // 1. Récupérer le campus existant s'il n'est pas supprimé
        Campus campus = campusRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Campus introuvable ou supprimé."));

        // 2. Vérification de l'unicité du code si celui-ci a changé
        if (dto.getCode() != null && !dto.getCode().equalsIgnoreCase(campus.getCode())) {
            if (campusRepository.existsByCode(dto.getCode())) {
                throw new IllegalArgumentException("Le code campus '" + dto.getCode() + "' est déjà utilisé.");
            }
            campus.setCode(dto.getCode());
        }

        // 3. Mise à jour des champs modifiables
        if (dto.getName() != null) campus.setName(dto.getName());
        if (dto.getAddress() != null) campus.setAddress(dto.getAddress());
        if (dto.getPhone() != null) campus.setPhone(dto.getPhone());
        if (dto.getCity() != null) campus.setCity(dto.getCity());
//        if (dto.getProvince() != null) campus.setProvince(dto.getProvince());
        if (dto.getCountry() != null) campus.setCountry(dto.getCountry());

        // 4. Sauvegarde et retour du DTO mis à jour
        Campus updatedCampus = campusRepository.save(campus);
        return mapToDto(updatedCampus);
    }

    private CampusResponseDto mapToDto(Campus campus) {
        return CampusResponseDto.builder()
                .id(campus.getId())
                .schoolId(campus.getSchool().getId())
                .schoolName(campus.getSchool().getName())
                .name(campus.getName())
                .code(campus.getCode()) // 👈 Décommenté pour que le frontend reçoive le code
                .address(campus.getAddress())
                .city(campus.getCity())
                .province(campus.getProvince()) // 👈 Ajouté pour mapper la province
                .country(campus.getCountry())
                .phone(campus.getPhone())
                .status(campus.getStatus())
                .createdAt(campus.getCreatedAt())
                .build();
    }


// UC-CAM-011 & UC-CAM-004 : Gestion des statuts (Activer, Suspendre, Archiver, Désactiver)
@Transactional
@Auditable(action = "UPDATE_STATUS", targetEntity = "CAMPUS")
public CampusResponseDto updateCampusStatus(UUID id, CampusStatus status) {
    Campus campus = campusRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new EntityNotFoundException("Campus introuvable"));
    
    campus.setStatus(status);
    return mapToDto(campusRepository.save(campus));
}

// UC-CAM-005 : Affecter un responsable
@Transactional
@Auditable(action = "ASSIGN_MANAGER", targetEntity = "CAMPUS")
public CampusResponseDto assignManager(UUID id, UUID managerId) {
    Campus campus = campusRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new EntityNotFoundException("Campus introuvable"));
    
    campus.setManagerId(managerId);
    return mapToDto(campusRepository.save(campus));
}

// UC-CAM-008 : Déclarer / Mettre à jour la capacité globale
@Transactional
public CampusResponseDto updateCapacity(UUID id, Integer capacity) {
    Campus campus = campusRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new EntityNotFoundException("Campus introuvable"));
    
    campus.setTotalCapacity(capacity);
    return mapToDto(campusRepository.save(campus));
}

// UC-CAM-006 : Ajouter un bâtiment
// Dans le Controller
@Transactional
@Auditable(action = "CREATE", targetEntity = "BUILDING")
public Building addingBuild(UUID campusId, BuildingRequestDto request) {
    // 1. Récupérer le campus existant (lève une 404 propre si introuvable)
    Campus campus = campusRepository.findByIdAndNotDeleted(campusId)
            .orElseThrow(() -> new EntityNotFoundException("Campus introuvable avec l'ID : " + campusId));

    // 2. Vérifier l'unicité du code bâtiment si nécessaire
    if (request.getCode() != null && buildingRepository.existsByCodeAndCampusId(request.getCode(), campusId)) {
        throw new IllegalArgumentException("Le code bâtiment '" + request.getCode() + "' existe déjà pour ce campus.");
    }

    // 3. Construction et sauvegarde
    Building building = Building.builder()
            .name(request.getName())
            .code(request.getCode())
            .campus(campus)
            .build();

    return buildingRepository.save(building);
}
// UC-CAM-007 & UC-CAM-009 : Rattacher une salle avec sa capacité et ses équipements
@Transactional
public Room addRoomToBuilding(UUID buildingId, RoomRequestDto dto) {
    Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new EntityNotFoundException("Bâtiment introuvable"));

    // Mappage des EquipmentRequest du DTO vers les objets Embeddable Equipment
    Set<Equipment> equipmentsSet = new HashSet<>();
    
    if (dto.getEquipments() != null) {
        equipmentsSet = dto.getEquipments().stream()
                .map(eq -> Equipment.builder()
                        .name(eq.getName())
                        .quantity(eq.getQuantity() != null ? eq.getQuantity() : 1)
                        .state(eq.getState() != null ? eq.getState() : "BON_ETAT")
                        .build())
                .collect(Collectors.toSet());
    }

    Room room = Room.builder()
            .building(building)
            .name(dto.getName())
            .capacity(dto.getCapacity())
            .equipments(equipmentsSet) // 👈 Compatible avec Set<Equipment>
            .build();

    return roomRepository.save(room);
}

@Transactional(readOnly = true)
public List<BuildingResponseDto> getBuildingsByCampus(UUID campusId) {

    if (campusRepository.findByIdAndNotDeleted(campusId).isEmpty()) {
        throw new EntityNotFoundException("Campus introuvable avec l'ID : " + campusId);
    }

    List<Building> buildings = buildingRepository.findByCampusIdWithRoomsAndEquipments(campusId);

    return buildings.stream()
            .map(building -> BuildingResponseDto.builder()
                    .id(building.getId())
                    .name(building.getName())
                    .code(building.getCode())
                    .campusId(campusId)
                    .rooms(building.getRooms() == null ? Collections.emptyList() : building.getRooms().stream()
                            .map(room -> RoomResponseDto.builder()
                                    .id(room.getId())
                                    .name(room.getName())
                                    .capacity(room.getCapacity())
                                    .buildingId(building.getId())
                                    // Mapping des objets Equipment vers EquipmentResponseDto
                                    .equipments(room.getEquipments() != null ? room.getEquipments().stream()
                                            .map(eq -> EquipmentResponseDto.builder()
                                                    .id(eq.getId()) // 👈 Inclusion de l'UUID de l'équipement
                                                    .name(eq.getName())
                                                    .quantity(eq.getQuantity())
                                                    .state(eq.getState())
                                                    .build())
                                            .toList() : Collections.emptyList())
                                    .build())
                            .toList())
                    .build())
            .toList();
}
@Transactional
public RoomResponseDto addEquipmentToRoom(UUID roomId, EquipmentRequestDto dto) {
    // 1. Récupération de la salle
    Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Salle introuvable avec l'ID : " + roomId));

    // 2. Instanciation de l'équipement et liaison avec la salle (OneToMany)
    Equipment equipment = Equipment.builder()
            .id(dto.getId())
            .name(dto.getName())
            .quantity(dto.getQuantity() != null ? dto.getQuantity() : 1)
            .state(dto.getState() != null ? dto.getState() : "BON_ETAT")
            .room(room) // 👈 Obligatoire pour populer la clé étrangère room_id
            .build();

    // 3. Sauvegarde directe dans la table des équipements
    Equipment savedEquipment = equipmentRepository.save(equipment);

    // 4. Ajout dans la collection locale de la salle
    if (room.getEquipments() == null) {
        room.setEquipments(new HashSet<>());
    }
    room.getEquipments().add(savedEquipment);

    // 5. Transformation de la liste pour le DTO de réponse
    List<EquipmentResponseDto> equipmentDtos = room.getEquipments().stream()
            .map(eq -> EquipmentResponseDto.builder()
                    .id(eq.getId()) // 👈 Inclusion de l'UUID de l'équipement
                    .name(eq.getName())
                    .quantity(eq.getQuantity())
                    .state(eq.getState())
                    .build())
            .toList();

    return RoomResponseDto.builder()
            .id(room.getId())
            .name(room.getName())
            .capacity(room.getCapacity())
            .buildingId(room.getBuilding() != null ? room.getBuilding().getId() : null)
            .equipments(equipmentDtos)
            .build();
}


@Transactional
public RoomResponseDto updateRoom(UUID roomId, RoomRequestDto request) {
    // 1. Récupération de la salle existante
    Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Salle introuvable avec l'ID : " + roomId));

    // 2. Mise à jour des informations de base
    room.setName(request.getName());
    room.setCapacity(request.getCapacity());

    // 3. Mise à jour synchronisée des équipements
    if (request.getEquipments() != null) {
        // Vider la collection (Hibernate supprimera les anciens équipements via orphanRemoval = true)
        room.getEquipments().clear();

        // Reconstruction des nouveaux équipements avec la référence vers la salle
        Set<Equipment> updatedEquipments = request.getEquipments().stream()
                .map(eqDto -> Equipment.builder()
                        .id(eqDto.getId())
                        .name(eqDto.getName())
                        .quantity(eqDto.getQuantity() != null ? eqDto.getQuantity() : 1)
                        .state(eqDto.getState() != null ? eqDto.getState() : "BON_ETAT")
                        .room(room) // 👈 Important : fixe la clé étrangère room_id
                        .build())
                .collect(Collectors.toSet());

        room.getEquipments().addAll(updatedEquipments);
    }

    // 4. Enregistrement des modifications
    Room savedRoom = roomRepository.save(room);

    // 5. Mapping des équipements pour la réponse DTO
    List<EquipmentResponseDto> equipmentDtos = savedRoom.getEquipments() != null
            ? savedRoom.getEquipments().stream()
                    .map(eq -> EquipmentResponseDto.builder()
                            .id(eq.getId())
                            .name(eq.getName())
                            .quantity(eq.getQuantity())
                            .state(eq.getState())
                            .build())
                    .toList()
            : Collections.emptyList();

    // 6. Retour de l'objet DTO complété
    return RoomResponseDto.builder()
            .id(savedRoom.getId())
            .name(savedRoom.getName())
            .capacity(savedRoom.getCapacity())
            .buildingId(savedRoom.getBuilding() != null ? savedRoom.getBuilding().getId() : null)
            .equipments(equipmentDtos)
            .build();
}

@Transactional
public Building updateBuilding(UUID buildingId, BuildingRequestDto request) {
    
    Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new EntityNotFoundException("Bâtiment introuvable avec l'ID : " + buildingId));

    building.setName(request.getName());
    building.setCode(request.getCode());
    return buildingRepository.save(building);
}


@Transactional
public void deleteEquipmentFromRoom(UUID roomId, UUID equipmentId) {
    // 1. Récupérer la salle par son ID (lancer une exception si introuvable)
    Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Salle introuvable avec l'ID : " + roomId));

    // 2. Vérifier si l'équipement existe dans la salle et le supprimer de la collection
    boolean removed = room.getEquipments().removeIf(equipment -> equipment.getId().equals(equipmentId));

    if (!removed) {
        throw new EntityNotFoundException("Équipement introuvable avec l'ID : " + equipmentId + " dans cette salle");
    }

    // 3. Si l'équipement est une entité dépendante avec 'orphanRemoval = true', la sauvegarde de la salle suffit.
    // Sinon, on peut également le supprimer directement depuis son repository :
    equipmentRepository.deleteById(equipmentId);

    // 4. Mettre à jour la salle
    roomRepository.save(room);
}

@Transactional
public RoomResponseDto updateEquipment(UUID roomId, UUID equipmentId, EquipmentRequestDto request) {
    // 1. Récupération de l'équipement à modifier
    Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new EntityNotFoundException("Équipement introuvable avec l'ID : " + equipmentId));

    // 2. Vérification que l'équipement appartient bien à la salle demandée
    if (equipment.getRoom() == null || !equipment.getRoom().getId().equals(roomId)) {
        throw new IllegalArgumentException("L'équipement " + equipmentId + " n'appartient pas à la salle " + roomId);
    }

    // 3. Mise à jour des champs de l'équipement
    if (request.getName() != null) {
        equipment.setName(request.getName());
    }
    if (request.getQuantity() != null) {
        equipment.setQuantity(request.getQuantity());
    }
    if (request.getState() != null) {
        equipment.setState(request.getState());
    }

    // 4. Sauvegarde des modifications
    equipmentRepository.save(equipment);

    // 5. Récupération de la salle mise à jour avec ses équipements
    Room room = equipment.getRoom();

    List<EquipmentResponseDto> equipmentDtos = room.getEquipments().stream()
            .map(eq -> EquipmentResponseDto.builder()
                    .id(eq.getId())
                    .name(eq.getName())
                    .quantity(eq.getQuantity())
                    .state(eq.getState())
                    .build())
            .toList();

    return RoomResponseDto.builder()
            .id(room.getId())
            .name(room.getName())
            .capacity(room.getCapacity())
            .buildingId(room.getBuilding() != null ? room.getBuilding().getId() : null)
            .equipments(equipmentDtos)
            .build();
}

}
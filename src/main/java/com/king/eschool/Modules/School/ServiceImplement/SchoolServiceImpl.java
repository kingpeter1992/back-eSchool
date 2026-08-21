package com.king.eschool.Modules.School.ServiceImplement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.Audite.Auditable;
import com.king.eschool.Modules.Cours.Repository.CourseRepository;
import com.king.eschool.Modules.Parent.repository.ParentRepository;
import com.king.eschool.Modules.School.Dto.SchoolStatus;
import com.king.eschool.Modules.School.Dto.reponse.CampusResponseDto;
import com.king.eschool.Modules.School.Dto.reponse.SchoolResponseDto;
import com.king.eschool.Modules.School.Dto.request.CampusRequestDto;
import com.king.eschool.Modules.School.Dto.request.SchoolRequestDto;
import com.king.eschool.Modules.School.Interfaces.ISchoolService;
import com.king.eschool.Modules.School.Models.Campus;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.CampusRepository;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.Modules.SchoolClasse.repository.schoolClassRepository;
import com.king.eschool.Modules.Student.repository.StudentRepository;
import com.king.eschool.Modules.Teach.TeacherRepository;
import com.king.eschool.shared.Storage.Services.FileStorageService;
import com.king.eschool.shared.Storage.dtoResponse.FileDocumentResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements ISchoolService {

    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final FileStorageService fileStorageService;
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
        FileDocumentResponse response = fileStorageService.uploadFile(file, "SCHOOL_LOGOS", refId);
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
        if (campusRepository.existsBySchoolIdAndDeletedAtIsNull(dto.getSchoolId())) {
            throw new IllegalArgumentException("Le code campus '" + dto.getCode() + "' existe déjà pour cette école.");
        }

        Campus campus = Campus.builder()
                .school(school)
                .name(dto.getName())
               // .code(dto.getCode())
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

    private CampusResponseDto mapToDto(Campus campus) {
        return CampusResponseDto.builder()
                .id(campus.getId())
                .schoolId(campus.getSchool().getId())
                .schoolName(campus.getSchool().getName())
                .name(campus.getName())
//                .code(campus.getCode())
                .address(campus.getAddress())
                .city(campus.getCity())
                .country(campus.getCountry())
                .phone(campus.getPhone())
                .status(campus.getStatus())
                .createdAt(campus.getCreatedAt())
                .build();
    }


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

}
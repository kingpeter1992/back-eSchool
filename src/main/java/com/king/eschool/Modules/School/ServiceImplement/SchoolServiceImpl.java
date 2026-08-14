package com.king.eschool.Modules.School.ServiceImplement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.Audite.Auditable;
import com.king.eschool.Modules.School.Dto.SchoolStatus;
import com.king.eschool.Modules.School.Dto.reponse.CampusResponse;
import com.king.eschool.Modules.School.Dto.reponse.SchoolResponseDto;
import com.king.eschool.Modules.School.Dto.request.CampusRequest;
import com.king.eschool.Modules.School.Dto.request.SchoolRequestDto;
import com.king.eschool.Modules.School.Interfaces.ISchoolService;
import com.king.eschool.Modules.School.Models.Campus;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.CampusRepository;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.shared.Storage.Services.FileStorageService;
import com.king.eschool.shared.Storage.dtoResponse.FileDocumentResponse;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements ISchoolService {

    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final FileStorageService fileStorageService;

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
        if (requestDto.getLogoFile() != null && !requestDto.getLogoFile().isEmpty()) {
            String logoUrl = processLogoUpload(savedSchool.getId(), requestDto.getLogoFile());
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
                .orElseThrow(() -> new RuntimeException("Établissement introuvable."));

        school.setName(requestDto.getName());
        school.setEmail(requestDto.getEmail());
        school.setPhone(requestDto.getPhone());
        if (requestDto.getCurrency() != null)
            school.setCurrency(requestDto.getCurrency());
        if (requestDto.getTimezone() != null)
            school.setTimezone(requestDto.getTimezone());
        if (requestDto.getDomain() != null)
            school.setDomain(requestDto.getDomain());

        // Si un nouveau fichier logo est envoyé lors de la modification
        if (requestDto.getLogoFile() != null && !requestDto.getLogoFile().isEmpty()) {
            // Suppression de l'ancien logo Supabase s'il existe
            if (school.getLogoUrl() != null && !school.getLogoUrl().isBlank()) {
                try {
                    fileStorageService.deleteFileByUrl(school.getLogoUrl());
                } catch (Exception ignored) {
                }
            }
            String newLogoUrl = processLogoUpload(school.getId(), requestDto.getLogoFile());
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
    @Auditable(action = "ADD_CAMPUS", targetEntity = "SCHOOL")
    public CampusResponse addCampus(UUID schoolId, CampusRequest request) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(schoolId)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable."));

        Campus campus = Campus.builder()
                .school(school)
                .name(request.name())
                .address(request.address())
                .phone(request.phone())
                .build();

        Campus savedCampus = campusRepository.save(campus);

        return new CampusResponse(
                savedCampus.getId(),
                savedCampus.getName(),
                savedCampus.getAddress(),
                savedCampus.getPhone());
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

    private SchoolResponseDto toDto(School school) {
        List<CampusResponse> campusList = school.getCampuses()
                .stream()
                .map(c -> new CampusResponse(
                        c.getId(),
                        c.getName(),
                        c.getAddress(),
                        c.getPhone()))
                .toList();

        SchoolResponseDto dto = new SchoolResponseDto();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setCode(school.getCode());
        dto.setEmail(school.getEmail());
        dto.setPhone(school.getPhone());
        dto.setCurrency(school.getCurrency());
        dto.setTimezone(school.getTimezone());
        dto.setDomain(school.getDomain());
        dto.setLogoUrl(school.getLogoUrl());
        dto.setStatus(school.getStatus());
        dto.setCampuses(campusList);
        return dto;
    }
}
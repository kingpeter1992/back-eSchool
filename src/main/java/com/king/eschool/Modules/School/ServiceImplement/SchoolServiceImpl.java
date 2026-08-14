package com.king.eschool.Modules.School.ServiceImplement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.eschool.Audite.Auditable;
import com.king.eschool.Modules.School.Dto.reponse.SchoolResponseDto;
import com.king.eschool.Modules.School.Dto.request.SchoolRequestDto;
import com.king.eschool.Modules.School.Interfaces.ISchoolService;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.SchoolRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SchoolServiceImpl implements ISchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolServiceImpl(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    @Override
    public List<SchoolResponseDto> getAllSchools() {
        return schoolRepository.findAll().stream()
                .filter(s -> s.getDeletedAt() == null)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SchoolResponseDto getSchoolById(UUID id) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable ou supprimé."));
        return toDto(school);
    }

    @Override
    @Transactional
    @Auditable(action = "CREATE", targetEntity = "SCHOOL")
    public SchoolResponseDto createSchool(SchoolRequestDto requestDto) {
        if (requestDto.getDomain() != null && schoolRepository.existsByDomain(requestDto.getDomain())) {
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
                .status(School.SchoolStatus.PENDING)
                .build();

        School savedSchool = schoolRepository.save(school);
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
        if (requestDto.getCurrency() != null) school.setCurrency(requestDto.getCurrency());
        if (requestDto.getTimezone() != null) school.setTimezone(requestDto.getTimezone());
        if (requestDto.getDomain() != null) school.setDomain(requestDto.getDomain());

        School updated = schoolRepository.save(school);
        return toDto(updated);
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE_STATUS", targetEntity = "SCHOOL")
    public SchoolResponseDto updateSchoolStatus(UUID id, String statusStr) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable."));

        School.SchoolStatus newStatus = School.SchoolStatus.valueOf(statusStr.toUpperCase());
        school.setStatus(newStatus);
        
        School updated = schoolRepository.save(school);
        return toDto(updated);
    }

    private SchoolResponseDto toDto(School school) {
        SchoolResponseDto dto = new SchoolResponseDto();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setCode(school.getCode());
        dto.setEmail(school.getEmail());
        dto.setPhone(school.getPhone());
        dto.setCurrency(school.getCurrency());
        dto.setTimezone(school.getTimezone());
        dto.setDomain(school.getDomain());
        dto.setStatus(school.getStatus());
        return dto;
    }

    @Override
    @Transactional
    @Auditable(action = "SOFT_DELETE", targetEntity = "SCHOOL")
    public void softDeleteSchool(UUID id) {
        School school = schoolRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable."));
        
        school.setDeletedAt(LocalDateTime.now());
        school.setStatus(School.SchoolStatus.DELETED);
        schoolRepository.save(school);
    }

    private String generateUniqueSchoolCode() {
        String code;
        do {
            int randomNum = (int) (Math.random() * 90000) + 10000;
            code = "SCH" + LocalDateTime.now().getYear() + randomNum;
        } while (schoolRepository.existsByCode(code));
        return code;
    }

}
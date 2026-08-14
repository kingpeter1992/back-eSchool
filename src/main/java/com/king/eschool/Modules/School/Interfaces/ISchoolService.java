package com.king.eschool.Modules.School.Interfaces;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.Modules.School.Dto.SchoolStatus;
import com.king.eschool.Modules.School.Dto.reponse.CampusResponse;
import com.king.eschool.Modules.School.Dto.reponse.SchoolResponseDto;
import com.king.eschool.Modules.School.Dto.request.CampusRequest;
import com.king.eschool.Modules.School.Dto.request.SchoolRequestDto;

public interface ISchoolService {
    List<SchoolResponseDto> getAllSchools();
    SchoolResponseDto getSchoolById(UUID id);
    SchoolResponseDto createSchool(SchoolRequestDto requestDto);
    SchoolResponseDto updateSchool(UUID id, SchoolRequestDto requestDto);
    SchoolResponseDto updateSchoolStatus(UUID id, String status);
    void softDeleteSchool(UUID id);
    List<SchoolResponseDto> getAllSchools(SchoolStatus status);
    CampusResponse addCampus(UUID schoolId, CampusRequest request);
    SchoolResponseDto uploadLogo(UUID schoolId, MultipartFile file);
}

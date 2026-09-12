package com.king.eschool.Modules.Admission.ServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.Audite.ServiceImpl.AuditService;
import com.king.eschool.Core.config.EmailServiceImpl;
import com.king.eschool.Modules.Academique.Models.AcademicYear;
import com.king.eschool.Modules.Academique.Models.SchoolClass;
import com.king.eschool.Modules.Academique.Repository.AcademicYearRepository;
import com.king.eschool.Modules.Academique.Repository.SchoolClassRepository;
import com.king.eschool.Modules.Admission.Dto.request.AssignClassRequestDTO;
import com.king.eschool.Modules.Admission.Dto.request.CreateEnrollmentRequestDTO;
import com.king.eschool.Modules.Admission.Dto.request.UpdateEnrollmentRequest;
import com.king.eschool.Modules.Admission.Dto.request.UpdateEnrollmentStatusRequestDTO;
import com.king.eschool.Modules.Admission.Dto.response.ClassCapacityDto;
import com.king.eschool.Modules.Admission.Dto.response.EnrollmentResponseDTO;
import com.king.eschool.Modules.Admission.Dto.response.EnrollmentStatusResponseDTO;
import com.king.eschool.Modules.Admission.Dto.response.EnrollmentSummaryResponse;
import com.king.eschool.Modules.Admission.Models.Enrollment;
import com.king.eschool.Modules.Admission.Models.EnrollmentDocument;
import com.king.eschool.Modules.Admission.Models.EnrollmentStatus;
import com.king.eschool.Modules.Admission.Models.PaymentInfoDTO;
import com.king.eschool.Modules.Admission.Repository.EnrollmentDocumentRepository;
import com.king.eschool.Modules.Admission.Repository.EnrollmentRepository;
import com.king.eschool.Modules.School.Models.Campus;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.CampusRepository;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.Utilities.TrackingTokenProvider;
import com.king.eschool.shared.Storage.Services.FileStorageService;
import com.king.eschool.shared.Storage.dtoResponse.FileDocumentResponse;
import com.king.eschool.shared.sms.SmsServiceImpl;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl {

    private final EnrollmentRepository enrollmentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private  final SchoolRepository schoolRepository;
    private  final CampusRepository campusRepository;
    private  final EmailServiceImpl emailServiceImpl;
    private final FileStorageService    fileStorageService; // Module Supabase Backend
    private final SmsServiceImpl smsService; // Injection du service SMS
    private  final EnrollmentDocumentRepository enrollmentDocumentRepository;
    private  final AcademicYearRepository academicYearRepository;
    private final AuditService auditService;

    private final TrackingTokenProvider trackingTokenProvider;
    
    @Value("${app.front-url}")
    private String appFrontUrl;

    @Transactional(readOnly = true)
    public EnrollmentResponseDTO getById(UUID id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inscription introuvable : " + id));
        return mapToResponse(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getAll() {
        return enrollmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getBySchool(UUID schoolId) {
        return enrollmentRepository.findBySchoolId(schoolId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getByAcademicYear(UUID academicYearId) {
        return enrollmentRepository.findByAcademicYearId(academicYearId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentResponseDTO updateStatus(UUID id, UpdateEnrollmentStatusRequestDTO request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inscription introuvable : " + id));

        EnrollmentStatus newStatus = request.getStatus();

        if (newStatus == EnrollmentStatus.ACCEPTED) {
            enrollment.setStatus(EnrollmentStatus.ACCEPTED);
            enrollment.setAdmissionDate(LocalDate.now());
        } else if (newStatus == EnrollmentStatus.REJECTED) {
            enrollment.setStatus(EnrollmentStatus.REJECTED);
            enrollment.setAdmissionDate(null);
        } else if (newStatus == EnrollmentStatus.ENROLLED) {
            if (enrollment.getRegistrationNo() == null || enrollment.getRegistrationNo().isBlank()) {
                String registrationNo = generateRegistrationNumber(
                        enrollment.getSchoolId(),
                        enrollment.getAcademicYearId());
                enrollment.setRegistrationNo(registrationNo);
            }
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            if (enrollment.getAdmissionDate() == null) {
                enrollment.setAdmissionDate(LocalDate.now());
            }
        } else if (newStatus == EnrollmentStatus.REVIEWING) {
            enrollment.setStatus(EnrollmentStatus.REVIEWING);
        } else if (newStatus == EnrollmentStatus.PENDING) {
            enrollment.setStatus(EnrollmentStatus.PENDING);
        }

        // Affectation de classe lors du changement de statut
        if (request.getClassId() != null) {
            AssignClassRequestDTO assignDTO = new AssignClassRequestDTO();
            assignDTO.setEnrollmentId(enrollment.getId());
            assignDTO.setClassId(request.getClassId());
            assignDTO.setOverrideCapacity(false);
            
            // Délègue la vérification de capacité et de statut à assignClass
            return assignClass(assignDTO);
        }
        

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentResponseDTO assignClass(AssignClassRequestDTO dto) {
        Enrollment enrollment = enrollmentRepository.findById(dto.getEnrollmentId())
                .orElseThrow(() -> new EntityNotFoundException("Dossier d'inscription introuvable"));

        // RG-ENR-001
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException("Seul un élève au statut ENROLLED peut être affecté.");
        }

        // RG-ENR-003 (Verrou pessimiste)
        SchoolClass targetClass = schoolClassRepository.findByIdForUpdate(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Classe introuvable"));

        int currentCount = enrollmentRepository.countByClassIdAndAcademicYearId(
                targetClass.getId(), enrollment.getAcademicYearId());

        if (currentCount >= targetClass.getMaxCapacity() && !dto.isOverrideCapacity()) {
            throw new IllegalStateException(
                "La classe " + targetClass.getName() + " est saturée. Dérogation administrative requise."
            );
        }

        enrollment.setClassId(targetClass.getId());
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<ClassCapacityDto> getAvailableClassesWithCapacity(UUID campusId, UUID levelId, UUID academicYearId) {
        List<SchoolClass> classes = schoolClassRepository.findByCampusIdAndLevelId(campusId, levelId);

        return classes.stream().map(sc -> {
            int current = enrollmentRepository.countByClassIdAndAcademicYearId(sc.getId(), academicYearId);
            int available = Math.max(0, sc.getMaxCapacity() - current);
            
            return ClassCapacityDto.builder()
                    .id(sc.getId())
                    .name(sc.getName())
                    .levelName(sc.getLevel() != null ? sc.getLevel().getName() : null)
                    .campusId(sc.getCampusId())
                    .maxCapacity(sc.getMaxCapacity())
                    .currentCount(current)
                    .availableSeats(available)
                    .isFull(current >= sc.getMaxCapacity())
                    .build();
        }).toList();
    }

    @Transactional
    public void delete(UUID id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inscription introuvable : " + id));
        enrollmentRepository.delete(enrollment);
    }
private String generateRegistrationNumber(UUID schoolId, UUID academicYearId) {
    String registrationNo;
    boolean exists;
    int maxAttempts = 10;
    int attempts = 0;

    do {
        // Exemple : ENR-2026-8902
        int randomSuffix = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 9999);
        int year = java.time.LocalDate.now().getYear();
        registrationNo = String.format("ENR-%d-%d", year, randomSuffix);

        exists = enrollmentRepository.existsByRegistrationNo(registrationNo);
        attempts++;

        if (attempts >= maxAttempts) {
            throw new IllegalStateException("Impossible de générer un numéro de dossier unique après plusieurs tentatives.");
        }
    } while (exists);

    return registrationNo;
}

@Transactional(readOnly = true)
public EnrollmentStatusResponseDTO getPublicStatus(String registrationNo) {

    // 1. Recherche du dossier par son numéro de matricule
    Enrollment enrollment = enrollmentRepository.findByRegistrationNo(registrationNo)
            .orElseThrow(() -> new EntityNotFoundException("Aucun dossier trouvé pour le numéro : " + registrationNo));

    // 2. Détermination de l'étape et du libellé selon le statut
    int stepNumber;
    String statusLabel;

    switch (enrollment.getStatus()) {
        case PENDING:
            stepNumber = 1;
            statusLabel = "Dossier Soumis";
            break;
        case REVIEWING:
            stepNumber = 2;
            statusLabel = "En cours d'examen";
            break;
        case ACCEPTED:
            stepNumber = 3;
            statusLabel = "Candidature Acceptée";
            break;
        case REJECTED:
            stepNumber = 3;
            statusLabel = "Candidature Refusée";
            break;
        default:
            stepNumber = 1;
            statusLabel = "En attente";
            break;
    }

    // 3. Formatage de la date de soumission
    String formattedDate = enrollment.getCreatedAt() != null 
            ? enrollment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            : "";

    // 4. Récupération sécurisée du nom de l'école et du campus via Optional
    String nameSchool = schoolRepository.findById(enrollment.getSchoolId())
            .map(School::getName)
            .orElse("eSchool");

    String nameCampus = campusRepository.findById(enrollment.getCampusId())
            .map(Campus::getName)
            .orElse("Principal");

    // 5. Construction et retour du DTO
    return EnrollmentStatusResponseDTO.builder()
            .registrationNo(enrollment.getRegistrationNo())
            .candidateFullName(enrollment.getCandidateFirstName() + " " + enrollment.getCandidateLastName())
            .submissionDate(formattedDate)
            .schoolName(nameSchool)
            .campusName(nameCampus)
            .status(enrollment.getStatus().name())
            .statusLabel(statusLabel)
            .stepNumber(stepNumber)
            .remarks(enrollment.getRemarks())
            .build();
}




    @Transactional
public EnrollmentResponseDTO create(
        CreateEnrollmentRequestDTO request, 
        MultipartFile photo, 
        List<MultipartFile> documents
) {
    String registrationNo = generateRegistrationNumber(request.getSchoolId(), request.getAcademicYearId());

    // 1. Instanciation de l'entité
    Enrollment enrollment = Enrollment.builder()
            .schoolId(request.getSchoolId())
            .campusId(request.getCampusId())
            .academicYearId(request.getAcademicYearId())
            .candidateFirstName(request.getCandidateFirstName())
            .candidatePostName(request.getCandidatePostName())
            .candidateLastName(request.getCandidateLastName())
            .gender(request.getGender())
            .candidateDateOfBirth(request.getCandidateDateOfBirth())
            .placeOfBirth(request.getPlaceOfBirth())
            .candidatePhone(request.getCandidatePhone())
            .candidateEmail(request.getCandidateEmail())
            .address(request.getAddress())
            .city(request.getCity())
            .maritalStatus(request.getMaritalStatus())
            .nationality(request.getNationality())
            .originVillage(request.getOriginVillage())
            .district(request.getDistrict())
            .territory(request.getTerritory())
            .parentFullName(request.getParentFullName())
            .parentPhone(request.getParentPhone())
            .parentAddress(request.getParentAddress())
            .vacation(request.getVacation())
            .cycleId(request.getCycleId())
            .levelId(request.getLevelId())
            .sectionId(request.getSectionId())
            .optionId(request.getOptionId())
            .targetClass(request.getTargetClass())
            .previousSchool(request.getPreviousSchool())
            .previousPercentage(request.getPreviousPercentage())
            .registrationNo(registrationNo)
            .status(EnrollmentStatus.PENDING)
            .build();

    Enrollment saved = enrollmentRepository.save(enrollment);

    // 2 & 3. Sauvegarde Photo et Documents (Inchangés)
    if (photo != null && !photo.isEmpty()) {
        FileDocumentResponse photoResponse = fileStorageService.uploadFile(photo, "enrollments/photos", saved.getId());
        saved.setPhotoUrl(photoResponse.getPublicUrl());
        saved = enrollmentRepository.save(saved);
    }

    if (documents != null && !documents.isEmpty()) {
        for (int i = 0; i < documents.size(); i++) {
            MultipartFile doc = documents.get(i);
            if (doc != null && !doc.isEmpty()) {
                FileDocumentResponse docResponse = fileStorageService.uploadFile(doc, "enrollments/documents", saved.getId());
                String docType = (request.getDocumentTypes() != null && i < request.getDocumentTypes().size())
                        ? request.getDocumentTypes().get(i) : "Document joint";

                EnrollmentDocument document = EnrollmentDocument.builder()
                        .enrollment(saved)
                        .documentType(docType)
                        .fileName(doc.getOriginalFilename())
                        .fileType(doc.getContentType())
                        .fileUrl(docResponse.getPublicUrl())
                        .build();

                saved.getDocuments().add(enrollmentDocumentRepository.save(document));
            }
        }
    }

    // --- GÉNÉRATION DU LIEN DE SUIVI (VALIDE 3 MOIS) ---
    String trackingToken = trackingTokenProvider.generateTrackingToken(registrationNo);
    String trackingUrl = appFrontUrl + "/verify-status?token=" + trackingToken;

    // 4. Notification Email avec le Lien direct
    if (request.getCandidateEmail() != null && !request.getCandidateEmail().isBlank()) {
        String fullName = request.getCandidateFirstName() + " " + request.getCandidateLastName();
        emailServiceImpl.sendEnrollmentConfirmationEmailWithLink(
                request.getCandidateEmail(), 
                fullName, 
                registrationNo, 
                trackingUrl
        );
    }

    // 5. Notification SMS avec le Lien
    if (request.getCandidatePhone() != null && !request.getCandidatePhone().isBlank()) {
        String messageSms = String.format(
                "Votre inscription eSchool (%s) est reçue. Suivez votre dossier via ce lien (valable 3 mois) : %s",
                registrationNo,
                trackingUrl
        );
        smsService.sendSms(request.getCandidatePhone(), messageSms);
    }

    return mapToResponse(saved);
}

    private EnrollmentResponseDTO mapToResponse(Enrollment enrollment) {

        String schoolName = schoolRepository.findById(enrollment.getSchoolId())
                .map(School::getName).orElse(null);
        String campusName = campusRepository.findById(enrollment.getCampusId())
                .map(Campus::getName).orElse(null);
        String yearName = academicYearRepository.findById(enrollment.getAcademicYearId())
                .map(AcademicYear::getName).orElse(null);
        String className = enrollment.getClassId() != null 
                ? schoolClassRepository.findById(enrollment.getClassId()).map(SchoolClass::getName).orElse(null)
                : null;

        List<EnrollmentResponseDTO.DocumentResponseDTO> docsDto = enrollment.getDocuments() != null
                ? enrollment.getDocuments().stream().map(doc -> EnrollmentResponseDTO.DocumentResponseDTO.builder()
                        .id(doc.getId())
                        .documentType(doc.getDocumentType())
                        .fileName(doc.getFileName())
                        .fileType(doc.getFileType())
                        .fileUrl(doc.getFileUrl())
                        .build()).collect(Collectors.toList())
                : List.of();

        return EnrollmentResponseDTO.builder()
                .id(enrollment.getId())
                .registrationNo(enrollment.getRegistrationNo())
                .status(enrollment.getStatus())
                .photoUrl(enrollment.getPhotoUrl())
                .remarks(enrollment.getRemarks())
                // Contexte
                .schoolId(enrollment.getSchoolId())
                .schoolName(schoolName)
                .campusId(enrollment.getCampusId())
                .campusName(campusName)
                .academicYearId(enrollment.getAcademicYearId())
                .academicYearName(yearName)
                .studentId(enrollment.getStudentId())
                .studentName(enrollment.getStudentId() != null 
                        ? "Nom Élève" 
                        : enrollment.getCandidateFirstName() + " " + enrollment.getCandidateLastName())
                .studentEmail(enrollment.getCandidateEmail())
                .classId(enrollment.getClassId())
                .className(className)
                // Informations Candidat
                .candidateFirstName(enrollment.getCandidateFirstName())
                .candidatePostName(enrollment.getCandidatePostName())
                .candidateLastName(enrollment.getCandidateLastName())
                .gender(enrollment.getGender())
                .candidateDateOfBirth(enrollment.getCandidateDateOfBirth())
                .placeOfBirth(enrollment.getPlaceOfBirth())
                .candidatePhone(enrollment.getCandidatePhone())
                .candidateEmail(enrollment.getCandidateEmail())
                .address(enrollment.getAddress())
                .city(enrollment.getCity())
                .maritalStatus(enrollment.getMaritalStatus())
                .nationality(enrollment.getNationality())
                .originVillage(enrollment.getOriginVillage())
                .district(enrollment.getDistrict())
                .territory(enrollment.getTerritory())
                // Parent
                .parentUserId(enrollment.getParentUserId())
                .parentFullName(enrollment.getParentFullName())
                .parentPhone(enrollment.getParentPhone())
                .parentAddress(enrollment.getParentAddress())
                // Orientation
                .vacation(enrollment.getVacation())
                .cycleId(enrollment.getCycleId())
                .levelId(enrollment.getLevelId())
                .sectionId(enrollment.getSectionId())
                .optionId(enrollment.getOptionId())
                .targetClass(enrollment.getTargetClass())
                .previousSchool(enrollment.getPreviousSchool())
                .previousPercentage(enrollment.getPreviousPercentage())
                // Règlement
                // .paymentReference(enrollment.getPaymentReference())
                // .paymentMethod(enrollment.getPaymentMethod())
                // .paymentPhoneOrCard(enrollment.getPaymentPhoneOrCard())
                // .amountPaid(enrollment.getAmountPaid())
                // Documents & Dates
                .documents(docsDto)
                .admissionDate(enrollment.getAdmissionDate())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}
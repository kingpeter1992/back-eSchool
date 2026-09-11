package com.king.eschool.Modules.Admission.ServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.king.eschool.Core.config.EmailServiceImpl;
import com.king.eschool.Modules.Academique.Models.SchoolClass;
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
import com.king.eschool.Modules.Admission.Repository.EnrollmentDocumentRepository;
import com.king.eschool.Modules.Admission.Repository.EnrollmentRepository;
import com.king.eschool.Modules.School.Models.Campus;
import com.king.eschool.Modules.School.Models.School;
import com.king.eschool.Modules.School.Repository.CampusRepository;
import com.king.eschool.Modules.School.Repository.SchoolRepository;
import com.king.eschool.shared.Storage.Services.FileStorageService;
import com.king.eschool.shared.Storage.dtoResponse.FileDocumentResponse;
import com.king.eschool.shared.sms.SmsServiceImpl;

import jakarta.persistence.EntityNotFoundException;

import java.lang.StackWalker.Option;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
    
    @Transactional
public EnrollmentResponseDTO create(
        CreateEnrollmentRequestDTO request, 
        MultipartFile photo, 
        List<MultipartFile> documents
) {

    // 1. Sauvegarde initiale pour générer l'ID (UUID ou Long) du candidat
    String registrationNo = generateRegistrationNumber(request.getSchoolId(), request.getAcademicYearId());

    Enrollment enrollment = Enrollment.builder()
            .schoolId(request.getSchoolId())
            .campusId(request.getCampusId())
            .academicYearId(request.getAcademicYearId())
            .candidateFirstName(request.getCandidateFirstName())
            .candidateLastName(request.getCandidateLastName())
            .candidateDateOfBirth(request.getCandidateDateOfBirth())
            .candidateEmail(request.getCandidateEmail())
            .candidatePhone(request.getCandidatePhone())
            .registrationNo(registrationNo)
            .status(EnrollmentStatus.PENDING)
            .build();

    Enrollment saved = enrollmentRepository.save(enrollment);

    // 2. Upload de la photo de profil reliée à l'ID du candidat
    if (photo != null && !photo.isEmpty()) {
        FileDocumentResponse photoResponse = fileStorageService.uploadFile(
                photo, 
                "enrollments/photos", 
                saved.getId() // L'ID réel du candidat au lieu de 0L
        );
        saved.setPhotoUrl(photoResponse.getPublicUrl());
        saved = enrollmentRepository.save(saved); // Mise à jour de l'URL photo
    }

    // 3. Upload des documents joints et sauvegarde en base
    if (documents != null && !documents.isEmpty()) {
        for (int i = 0; i < documents.size(); i++) {
            MultipartFile doc = documents.get(i);
            
            if (doc != null && !doc.isEmpty()) {
                // Stockage sur Supabase dans un dossier identifié par l'ID du candidat
                FileDocumentResponse docResponse = fileStorageService.uploadFile(
                        doc, 
                        "enrollments/documents", 
                        saved.getId()
                );

                // Récupération du type de document correspondant depuis le DTO (ex: "Diplôme", "Actes")
                String docType = (request.getDocumentTypes() != null && i < request.getDocumentTypes().size())
                ? request.getDocumentTypes().get(i)
                : "Document joint";

                // Création du lien fort en BDD entre le document et le candidat
                EnrollmentDocument document = EnrollmentDocument.builder()
                        .enrollment(saved) // Clé étrangère vers l'entité Enrollment
                        .documentType(docType)
                        .fileName(doc.getOriginalFilename())
                        .fileType(doc.getContentType())
                        .fileUrl(docResponse.getPublicUrl()) // URL publique distante Supabase
                        .build();

                enrollmentDocumentRepository.save(document);
            }
        }
    }

    // 4. Notification Email
    if (request.getCandidateEmail() != null && !request.getCandidateEmail().isBlank()) {
        String fullName = request.getCandidateFirstName() + " " + request.getCandidateLastName();
        emailServiceImpl.sendEnrollmentConfirmationEmail(request.getCandidateEmail(), fullName, registrationNo);
    }

    // 5. Notification SMS (Twilio)
    if (request.getCandidatePhone() != null && !request.getCandidatePhone().isBlank()) {
        smsService.sendEnrollmentNotificationSms(
            request.getCandidatePhone(), 
            registrationNo, 
            LocalDate.now()
        );
    }

    return mapToResponse(saved);
}

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
        long count = enrollmentRepository.countByClassIdAndAcademicYearId(schoolId, academicYearId);
        long sequence = count + 1;
        return String.format("ECOLE%d%04d", LocalDate.now().getYear(), sequence);
    }

    private EnrollmentResponseDTO mapToResponse(Enrollment enrollment) {
        return EnrollmentResponseDTO.builder()
                .id(enrollment.getId())
                .schoolId(enrollment.getSchoolId())
                .campusId(enrollment.getCampusId())
                .studentId(enrollment.getStudentId())
                .studentName(enrollment.getStudentId() != null ? 
                        "Nom Élève" : enrollment.getCandidateFirstName() + " " + enrollment.getCandidateLastName())
                .classId(enrollment.getClassId())
                .academicYearId(enrollment.getAcademicYearId())
                .registrationNo(enrollment.getRegistrationNo())
                .status(enrollment.getStatus())
                .admissionDate(enrollment.getAdmissionDate())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
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
}
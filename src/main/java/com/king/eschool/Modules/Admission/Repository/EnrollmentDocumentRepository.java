package com.king.eschool.Modules.Admission.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.Admission.Models.EnrollmentDocument;

public interface EnrollmentDocumentRepository extends  JpaRepository<EnrollmentDocument,Long>{
    
}

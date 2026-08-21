package com.king.eschool.Modules.Student.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.Student.models.Student;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Long countBySchoolId(UUID id);

    
    
}

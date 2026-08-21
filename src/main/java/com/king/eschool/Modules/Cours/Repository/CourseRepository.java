package com.king.eschool.Modules.Cours.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.Cours.Models.Cours;

public interface CourseRepository extends JpaRepository<Cours, UUID> {

    Long countBySchoolId(UUID id);
   
}

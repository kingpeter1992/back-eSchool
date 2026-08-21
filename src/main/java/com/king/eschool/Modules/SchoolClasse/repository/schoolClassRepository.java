package com.king.eschool.Modules.SchoolClasse.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.SchoolClasse.models.schoolClass;

public interface schoolClassRepository extends JpaRepository<schoolClass, UUID> {

    static Long countBySchoolId(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'countBySchoolId'");
    }
    
}

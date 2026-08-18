package com.king.eschool.Modules.School.Repository;

import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.eschool.Modules.School.Dto.SchoolStatus;
import com.king.eschool.Modules.School.Models.School;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolRepository extends JpaRepository<School, UUID> {
    boolean existsByCode(String code);
    boolean existsByDomain(String domain);
    Optional<School> findByIdAndDeletedAtIsNull(UUID id);    
    List<School> findByDeletedAtIsNull();
    List<School> findByStatusAndDeletedAtIsNull(SchoolStatus status);
    boolean existsByIdAndDeletedAtIsNull(UUID uuid);
}

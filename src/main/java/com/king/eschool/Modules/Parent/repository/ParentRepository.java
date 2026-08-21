package com.king.eschool.Modules.Parent.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.Parent.Models.Parent;

public interface ParentRepository extends JpaRepository<Parent,UUID>{

    Long countBySchoolId(UUID id);

    
}
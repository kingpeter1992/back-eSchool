package com.king.eschool.Modules.Teach;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.Teach.models.Teacher;

public interface TeacherRepository  extends JpaRepository<Teacher,UUID>{

    Long countBySchoolId(UUID id);
    
}

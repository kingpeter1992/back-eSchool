package com.king.eschool.Modules.School.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.eschool.Modules.School.Models.Room;

public interface RoomRepository  extends JpaRepository<Room,UUID>{
    
}

package com.king.eschool.Modules.Academique.Models;

import java.util.UUID;

import com.king.eschool.Modules.Academique.Enum.ClassStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "school_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClass {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID schoolId;

  @Column(nullable = false)
  private UUID campusId;

  @Column(name = "room_id")
  private UUID roomId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "level_id", nullable = false)
  private AcademicLevel level;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false)
  private Integer maxCapacity = 40;

  private UUID mainTeacherId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ClassStatus status = ClassStatus.OPEN;

  private boolean deleted = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "shift")
  private ShiftType shift; // MORNING ou AFTERNOON
}

package com.king.eschool.Modules.Academique.Models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "academic_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AcademicOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private AcademicSection section;

    @Column(nullable = false)
    private String name; // ex: Commerciale & Gestion, Math-Physique

    @Column(nullable = false)
    private String code; // ex: COM-GES, MAT-PHY

    @OneToMany(mappedBy = "option")
    private List<AcademicLevel> levels = new ArrayList<>();
}
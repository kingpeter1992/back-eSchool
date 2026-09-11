package com.king.eschool.Modules.Academique.Models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "academic_levels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AcademicLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private AcademicCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private AcademicOption option; // Optionnel (RG-AST-002)

    @Column(nullable = false)
    private String name; // ex: 1ère Année, 3ème Humanités

    @Column(name = "numeric_order", nullable = false)
    private Integer numericOrder;
}